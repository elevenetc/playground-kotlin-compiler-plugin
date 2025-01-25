import org.jetbrains.kotlin.CallLogger
import org.jetbrains.kotlin.addCallLog.AddCallLogCommandLineProcessor
import org.jetbrains.kotlin.addCallLog.AddCallLogPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import utils.*
import utils.reflection.call
import utils.reflection.newInstance
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalCompilerApi::class)
class AddCallLogPluginTest {

    @Rule
    @JvmField
    var tempDir = TemporaryFolder()

    @Before
    fun before() {
        CallLogger.instance.enableBump = false
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
            processor = AddCallLogCommandLineProcessor(),
            options = {
                listOf(
                    option(AddCallLogCommandLineProcessor.EXCLUDED_FQNS.option, listOf("Foo.bar0"))
                )
            }
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
            processor = AddCallLogCommandLineProcessor(),
            options = {
                listOf(
                    option(AddCallLogCommandLineProcessor.EXCLUDED_FQNS.option, listOf("Foo.bar*"))
                )
            }
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
            processor = AddCallLogCommandLineProcessor(),
            options = {
                listOf(
                    option(AddCallLogCommandLineProcessor.EXCLUDED_FQNS.option, listOf("Foo.test*", "Bar.*"))
                )
            }
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
    fun `test - single return`() {
        val source = """
            class Foo {
                fun bar(): Int {
                    return 42
                }
            }
        """.trimIndent()

        val result = compile(
            sourceInfo = buildSourceInfo(tempDir, source),
            registrar = AddCallLogPluginRegistrar()
        ).also { result -> result.assertSuccess() }

        val expected = """
            public final class Foo {
                public final int bar() {
                    Uuid uuid = CallLogger.Companion.getInstance().start("Foo.bar");
                    CallLogger.Companion.getInstance().end(uuid);
                    return 42;
                }
            }
        """.trimIndent()

        assertEquals(expected, result.decompileClassAndTrim("Foo.class"))
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
            registrar = AddCallLogPluginRegistrar()
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

    @OptIn(ExperimentalUuidApi::class)
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
            registrar = AddCallLogPluginRegistrar()
        ).also { result -> result.assertSuccess() }

        val expected = """
            public final class SourceKt {
                @NotNull
                private static final Foo foo;
                @NotNull
                public static final Foo getFoo() {
                    Uuid uuid = CallLogger.Companion.getInstance().start("<get-foo>");
                    Foo foo = SourceKt.foo;
                    CallLogger.Companion.getInstance().end(uuid);
                    return foo;
                }
                private static final Unit foo${'$'}lambda${'$'}1${'$'}lambda${'$'}0() {
                    Uuid uuid = CallLogger.Companion.getInstance().start("foo.<anonymous>.<anonymous>");
                    Unit unit = Unit.INSTANCE;
                    CallLogger.Companion.getInstance().end(uuid);
                    return unit;
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

        assertEquals(expected, result.decompileClassAndTrim("SourceKt.class"))
    }

    @OptIn(ExperimentalUuidApi::class)
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
            classpath = listOf(File(tempDir.root, "classes"))
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
        val foo = loader.loadClass("Foo").kotlin.newInstance()
        val calls = CallLogger.instance.calls

        calls.assertEmpty()
        foo.call("bar")
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
}
