import org.jetbrains.kotlin.CallLogger
import org.jetbrains.kotlin.addCallLog.AddCallLogCommandLineProcessor
import org.jetbrains.kotlin.addCallLog.AddCallLogCommandLineProcessor.Companion.ENABLE_CALLS_TRACING
import org.jetbrains.kotlin.addCallLog.AddCallLogCommandLineProcessor.Companion.EXCLUDED_FILES
import org.jetbrains.kotlin.addCallLog.AddCallLogCommandLineProcessor.Companion.EXCLUDED_FQN
import org.jetbrains.kotlin.addCallLog.AddCallLogPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import utils.*
import utils.reflection.call
import utils.reflection.newAnyInstance
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalCompilerApi::class, ExperimentalUuidApi::class)
class AddCallLogPluginTest {

    @Rule
    @JvmField
    var tempDir = TemporaryFolder()

    @Before
    fun before() {
        CallLogger.instance.enableDump = false
    }

    @Test
    fun `explicit and implicit return Unit types`() {
        val source = """
            fun fooReturnUnit(): Unit {
                println("1")
                println("2")
                return
            }
            
            fun fooReturnNothing() {
                println("1")
                println("2")
            }
        """.trimIndent()

        val result = compile(
            sourceInfo = buildSourceInfo(tempDir, source),
            registrar = AddCallLogPluginRegistrar(),
            processors = mapOf(
                AddCallLogCommandLineProcessor() to listOf(
                    ENABLE_CALLS_TRACING.option to true
                )
            )
        ).also { result -> result.assertSuccess() }

        val expected = """
            public final class SourceKt {
                public static final void fooReturnUnit() {
                    Uuid uuid = CallLogger.Companion.getInstance().start("fooReturnUnit");
                    System.out.println((Object)"1");
                    System.out.println((Object)"2");
                    CallLogger.Companion.getInstance().end(uuid);
                }
                public static final void fooReturnNothing() {
                    Uuid uuid = CallLogger.Companion.getInstance().start("fooReturnNothing");
                    System.out.println((Object)"1");
                    System.out.println((Object)"2");
                    CallLogger.Companion.getInstance().end(uuid);
                }
            }
        """.trimIndent()

        val actual = result.decompileClassAndTrim("SourceKt.class")
        assertEqualsCode(expected, actual)
    }

    @Test
    fun `test - simple function`() {
        val source = """
            fun foo() {

            }
        """.trimIndent()

        val result = compile(
            sourceInfo = buildSourceInfo(tempDir, source),
            registrar = AddCallLogPluginRegistrar(),
            processors = mapOf(
                AddCallLogCommandLineProcessor() to listOf(
                    ENABLE_CALLS_TRACING.option to true
                )
            )
        ).also { result -> result.assertSuccess() }

        val expected = """
            public final class SourceKt {
                public static final void foo() {
                    Uuid uuid = CallLogger.Companion.getInstance().start("foo");
                    CallLogger.Companion.getInstance().end(uuid);
                }
            }
        """.trimIndent()

        val actual = result.decompileClassAndTrim("SourceKt.class")
        assertEqualsCode(expected, actual)
    }

    @Test
    fun `test - simple function return simple value`() {
        val source = """
            fun bar(): Int {
                return 42
            }
        """.trimIndent()

        val result = compile(
            sourceInfo = buildSourceInfo(tempDir, source),
            registrar = AddCallLogPluginRegistrar(),
            processors = mapOf(
                AddCallLogCommandLineProcessor() to listOf(
                    ENABLE_CALLS_TRACING.option to true
                )
            )
        ).also { result -> result.assertSuccess() }

        val expected = """
            public final class SourceKt {
                public static final int bar() {
                    Uuid uuid = CallLogger.Companion.getInstance().start("bar");
                    CallLogger.Companion.getInstance().end(uuid);
                    return 42;
                }
            }
        """.trimIndent()

        val actual = result.decompileClassAndTrim("SourceKt.class")
        assertEqualsCode(expected, actual)
    }

    @Test
    fun `test - ignore annotation`() {
        val source = """
            import org.jetbrains.kotlin.IgnoreCallLog

            class Foo {
                @IgnoreCallLog
                fun shouldBeIgnored() {
                    println("should be ignored")
                }

                fun notIgnored() {
                    println("not ignored")
                }
            }
            
            @IgnoreCallLog
            class Bar {            
                fun shouldBeIgnored() { 
                }
            }
            
        """.trimIndent()

        val result = compile(
            sourceInfo = buildSourceInfo(tempDir, source),
            registrar = AddCallLogPluginRegistrar(),
            processors = mapOf(
                AddCallLogCommandLineProcessor() to listOf(
                    ENABLE_CALLS_TRACING.option to true
                )
            )
        ).also { result -> result.assertSuccess() }

        val expectedFoo = """
            public final class Foo {
                @IgnoreCallLog
                public final void shouldBeIgnored() {
                    System.out.println((Object)"should be ignored");
                }
                public final void notIgnored() {
                    Uuid uuid = CallLogger.Companion.getInstance().start("Foo.notIgnored");
                    System.out.println((Object)"not ignored");
                    CallLogger.Companion.getInstance().end(uuid);
                }
            }
        """.trimIndent()

        val expectedBar = """
            @IgnoreCallLog
            public final class Bar {
                public final void shouldBeIgnored() {
                }
            }
        """.trimIndent()

        assertEqualsCode(expectedFoo, result.decompileClassAndTrim("Foo.class"))
        assertEqualsCode(expectedBar, result.decompileClassAndTrim("Bar.class"))
    }

    @Test
    fun `test - exclude fqn`() {
        val source = """
            class Foo {
                fun bar0() {

                }
                fun bar1() {

                }
            }
        """.trimIndent()

        val result = compile(
            sourceInfo = buildSourceInfo(tempDir, source),
            registrar = AddCallLogPluginRegistrar(),

            processors = mapOf(
                AddCallLogCommandLineProcessor() to listOf(
                    ENABLE_CALLS_TRACING.option to true,
                    EXCLUDED_FQN.option to "Foo.bar0"
                )
            )
        ).also { result -> result.assertSuccess() }

        val expected = """
            public final class Foo {
                public final void bar0() {
                }
                public final void bar1() {
                    Uuid uuid = CallLogger.Companion.getInstance().start("Foo.bar1");
                    CallLogger.Companion.getInstance().end(uuid);
                }
            }
        """.trimIndent()

        assertEquals(expected, result.decompileClassAndTrim("Foo.class"))
    }

    @Test
    fun `test - exclude fqn with wildcard`() {
        val source = """
            class Foo {
                fun bar0() {

                }
                fun bar1() {

                }
                fun other() {

                }
            }
        """.trimIndent()

        val result = compile(
            sourceInfo = buildSourceInfo(tempDir, source),
            registrar = AddCallLogPluginRegistrar(),

            processors = mapOf(
                AddCallLogCommandLineProcessor() to listOf(
                    ENABLE_CALLS_TRACING.option to true,
                    EXCLUDED_FQN.option to "Foo.bar*"
                )
            )
        ).also { result -> result.assertSuccess() }

        val expected = """
            public final class Foo {
                public final void bar0() {
                }
                public final void bar1() {
                }
                public final void other() {
                    Uuid uuid = CallLogger.Companion.getInstance().start("Foo.other");
                    CallLogger.Companion.getInstance().end(uuid);
                }
            }
        """.trimIndent()

        assertEquals(expected, result.decompileClassAndTrim("Foo.class"))
    }

    @Test
    fun `test - exclude multiple patterns`() {
        val source = """
            class Foo {
                fun test1() {

                }
                fun test2() {

                }
                fun other() {

                }
            }
            class Bar {
                fun test() {

                }
            }
        """.trimIndent()

        val result = compile(
            sourceInfo = buildSourceInfo(tempDir, source),
            registrar = AddCallLogPluginRegistrar(),

            processors = mapOf(
                AddCallLogCommandLineProcessor() to listOf(
                    ENABLE_CALLS_TRACING.option to true,
                    EXCLUDED_FQN.option to "Foo.test*",
                    EXCLUDED_FQN.option to "Bar.*"
                )
            )
        ).also { result -> result.assertSuccess() }

        val expected = """
            public final class Foo {
                public final void test1() {
                }
                public final void test2() {
                }
                public final void other() {
                    Uuid uuid = CallLogger.Companion.getInstance().start("Foo.other");
                    CallLogger.Companion.getInstance().end(uuid);
                }
            }
        """.trimIndent()

        assertEquals(expected, result.decompileClassAndTrim("Foo.class"))

        val expectedBar = """
            public final class Bar {
                public final void test() {
                }
            }
        """.trimIndent()

        assertEquals(expectedBar, result.decompileClassAndTrim("Bar.class"))
    }

    @Test
    fun `test - multiple returns`() {
        val source = """
            class Foo {
                fun bar(value: Boolean): Int {
                    if(value) {
                        return 1
                    } else {
                        return 2    
                    }
                }
            }
        """.trimIndent()

        val result = compile(
            sourceInfo = buildSourceInfo(tempDir, source),
            registrar = AddCallLogPluginRegistrar(),
            processors = mapOf(
                AddCallLogCommandLineProcessor() to listOf(
                    ENABLE_CALLS_TRACING.option to true
                )
            )
        ).also { result -> result.assertSuccess() }

        val expected = """
            public final class Foo {
                public final int bar(boolean value) {
                    Uuid uuid = CallLogger.Companion.getInstance().start("Foo.bar");
                    if (value) {
                        CallLogger.Companion.getInstance().end(uuid);
                        return 1;
                    }
                    CallLogger.Companion.getInstance().end(uuid);
                    return 2;
                }
            }
        """.trimIndent()

        assertEquals(expected, result.decompileClassAndTrim("Foo.class"))
    }

    @Test
    fun `test - lambda control flow`() {
        val source = """
            class Foo {
                fun bar(f: () -> Unit) {
                    f()
                }
            }


            val foo = Foo().apply { 
                this.bar {

                }
            }
        """.trimIndent()

        val result = compile(
            sourceInfo = buildSourceInfo(tempDir, source),
            registrar = AddCallLogPluginRegistrar(),
            processors = mapOf(
                AddCallLogCommandLineProcessor() to listOf(
                    ENABLE_CALLS_TRACING.option to true
                )
            )
        ).also { result -> result.assertSuccess() }

        val expected = """
            public final class SourceKt {
                @NotNull
                private static final Foo foo;
                @NotNull
                public static final Foo getFoo() {
                    Uuid uuid = CallLogger.Companion.getInstance().start("<get-foo>");
                    CallLogger.Companion.getInstance().end(uuid);
                    return foo;
                }
                private static final Unit foo${'$'}lambda${'$'}1${'$'}lambda${'$'}0() {
                    Uuid uuid = CallLogger.Companion.getInstance().start("foo.<anonymous>.<anonymous>");
                    CallLogger.Companion.getInstance().end(uuid);
                    return Unit.INSTANCE;
                }
                static {
                    Foo foo;
                    Foo ${'$'}this${'$'}foo_u24lambda_u241 = foo = new Foo();
                    boolean bl = false;
                    Uuid uuid = CallLogger.Companion.getInstance().start("foo.<anonymous>");
                    ${'$'}this${'$'}foo_u24lambda_u241.bar(SourceKt::foo${'$'}lambda${'$'}1${'$'}lambda${'$'}0);
                    CallLogger.Companion.getInstance().end(uuid);
                    SourceKt.foo = foo;
                }
            }
        """.trimIndent()

        val actual = result.decompileClassAndTrim("SourceKt.class")
        assertEquals(expected, actual)
    }

    @Test
    fun `test - log calls injected in the beginning and the end of body`() {

        val fooSource = """
            class Foo {
             fun bar() {
                 val mid = 42
                 Thread.sleep(1)
             }
            }
        """.trimIndent()

        val result = compile(
            sourceInfo = buildSourceInfo(tempDir, fooSource),
            registrar = AddCallLogPluginRegistrar(),
            classpath = listOf(File(tempDir.root, "classes")),
            processors = mapOf(
                AddCallLogCommandLineProcessor() to listOf(
                    ENABLE_CALLS_TRACING.option to true
                )
            )
        ).also { result -> result.assertSuccess() }

        assertEquals(
            """
            public final class Foo {
                public final void bar() {
                    Uuid uuid = CallLogger.Companion.getInstance().start("Foo.bar");
                    int mid = 42;
                    Thread.sleep(1L);
                    CallLogger.Companion.getInstance().end(uuid);
                }
            }
        """.trimIndent(), result.decompileClassAndTrim("Foo.class")
        )

        val loader = result.classLoader
        val foo = loader.loadClass("Foo").kotlin.newAnyInstance()
        val threads = CallLogger.instance.threads

        threads.assertEmpty()
        foo.call("bar")

        val calls = threads.values.first().calls

        calls.assertSizeOf(1)
        foo.call("bar")
        calls.assertSizeOf(2)

        calls.apply {
            val firstCall = this[keys.first()] ?: error("First call " + keys.first() + " does not exist")
            val secondCall = this[keys.second()] ?: error("Second call " + keys.first() + " does not exist")

            assertEquals(firstCall.fqn, secondCall.fqn)
            assertEquals("Foo.bar", secondCall.fqn)
            assertTrue(firstCall.start <= firstCall.end)
            assertTrue(secondCall.start <= secondCall.end)
            assertTrue(firstCall.start <= secondCall.start)
        }
    }

    @Test
    fun `test - inline lambda return`() {
        val source = """
            fun foo() {
                bar {
                    return
                }
            }
            
            inline fun bar(lambda: () -> Unit) {
                lambda()
            }
        """.trimIndent()

        val result = compile(
            sourceInfo = buildSourceInfo(tempDir, source),
            registrar = AddCallLogPluginRegistrar(),
            processors = mapOf(
                AddCallLogCommandLineProcessor() to listOf(
                    ENABLE_CALLS_TRACING.option to true
                )
            )
        ).also { result -> result.assertSuccess() }

        val actual = result.decompileClassAndTrim("SourceKt.class")

        val expected = """
            public final class SourceKt {
                public static final void foo() {
                    Uuid uuid = CallLogger.Companion.getInstance().start("foo");
                    boolean ${'$'}i${'$'}f${'$'}bar = false;
                    CallLogger.Companion.getInstance().start("bar");
                    boolean bl = false;
                    CallLogger.Companion.getInstance().start("foo.<anonymous>");
                    CallLogger.Companion.getInstance().end(uuid);
                }
                public static final void bar(@NotNull Function0<Unit> lambda) {
                    Intrinsics.checkNotNullParameter(lambda, "lambda");
                    boolean ${'$'}i${'$'}f${'$'}bar = false;
                    Uuid uuid = CallLogger.Companion.getInstance().start("bar");
                    lambda.invoke();
                    CallLogger.Companion.getInstance().end(uuid);
                }
            }
        """.trimIndent()

        assertEqualsCode(expected, actual)
    }

    @Test
    fun `test - exclude by file name`() {
        val source = """
            fun foo() {

            }
        """.trimIndent()

        val fileName = "foo.kt"
        val result = compile(
            sourceInfo = buildSourceInfo(tempDir, source, fileName),
            registrar = AddCallLogPluginRegistrar(),

            processors = mapOf(
                AddCallLogCommandLineProcessor() to listOf(
                    ENABLE_CALLS_TRACING.option to true,
                    EXCLUDED_FILES.option to fileName
                )
            )
        ).also { result -> result.assertSuccess() }

        val expected = """
            public final class FooKt {
                public static final void foo() {
                }
            }
        """.trimIndent()

        val actual = result.decompileClassAndTrim("FooKt.class")
        assertEqualsCode(expected, actual)
    }
}
