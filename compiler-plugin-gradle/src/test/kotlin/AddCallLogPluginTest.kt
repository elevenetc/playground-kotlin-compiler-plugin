import org.jetbrains.kotlin.addCallLog.AddCallLogPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetrbains.kotlin.CallLogger
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