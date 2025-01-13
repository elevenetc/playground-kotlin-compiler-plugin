import addCallLog.AddCallLogPluginRegistrar
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

        result.printCompiledClass("Foo.class")

        assertEquals(
            """
            import kotlin.uuid.Uuid;
            import org.jetrbains.kotlin.CallLogger;
            public final class Foo {
                public final void bar() {
                    Uuid uuid = CallLogger.Companion.getInstance().start("/Foo.bar");
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
            assertEquals("/Foo.bar", secondCall.fqn)
            assertTrue(firstCall.start <= firstCall.end)
            assertTrue(secondCall.start <= secondCall.end)
            assertTrue(firstCall.start <= secondCall.start)
        }
    }
}