import addCallLog.AddCallLogPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import utils.*
import utils.reflection.call
import utils.reflection.get
import utils.reflection.getCompanionProperty
import utils.reflection.newInstance
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalCompilerApi::class)
class AddCallLogPluginTest {

    @Rule
    @JvmField
    var tempDir = TemporaryFolder()

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `test - log calls injected in the beginning and the end of body`() {

        val loggerSource = """
            import kotlin.uuid.ExperimentalUuidApi            
            import kotlin.uuid.Uuid

            @OptIn(ExperimentalUuidApi::class)
            class CallLogger {
            
                val calls = mutableMapOf<Uuid, Call>()
            
                fun start(callFqn: String): Uuid {
                    val call = Call(Uuid.random(), callFqn, System.currentTimeMillis(), -1)
                    calls[call.id] = call
                    return call.id
                }
            
                fun end(id: Uuid) {
                    val call = calls[id] ?: error("Call " + id + " does not exist")
                    calls[id] = call.copy(end = System.currentTimeMillis())
                }
            
                data class Call(
                    val id: Uuid,
                    val fqn: String,
                    val start: Long,
                    val end: Long
                )
            
                companion object {
                    @JvmStatic
                    val instance = CallLogger()
                }
            }


        """.trimIndent()

        val fooSource = """
            class Foo {
             fun bar() {
                 val mid = 42
                 Thread.sleep(1)
             }
            }
        """.trimIndent()

        compile(buildSourceInfo(tempDir, loggerSource)).assertSuccess()

        val result = compile(
            sourceInfo = buildSourceInfo(tempDir, fooSource),
            registrar = AddCallLogPluginRegistrar(),
            classpath = listOf(File(tempDir.root, "classes"))
        ).also { result -> result.assertSuccess() }

        result.printCompiledClass("Foo.class")

        assertEquals(
            """
            import kotlin.uuid.Uuid;
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
        val logger = loader.loadClass("CallLogger").kotlin.getCompanionProperty("instance")
        fun getCalls(): Map<Uuid, CallLogger.Call> = logger.get<LinkedHashMap<Uuid, CallLogger.Call>>("calls")

        getCalls().assertEmpty()
        foo.call("bar")
        getCalls().assertSizeOf(1)
        foo.call("bar")
        getCalls().assertSizeOf(2)

        getCalls().apply {
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

@OptIn(ExperimentalUuidApi::class)
class CallLogger {

    val calls = mutableMapOf<Uuid, Call>()

    fun start(callFqn: String): Uuid {
        val call = Call(Uuid.random(), callFqn, System.currentTimeMillis(), -1)
        calls[call.id] = call
        return call.id
    }

    fun end(id: Uuid) {
        val call = calls[id] ?: error("Call $id does not exist")
        calls[id] = call.copy(end = System.currentTimeMillis())
    }

    data class Call(
        val id: Uuid,
        val fqn: String,
        val start: Long,
        val end: Long
    )

    companion object {
        @JvmStatic
        val instance = CallLogger()
    }
}