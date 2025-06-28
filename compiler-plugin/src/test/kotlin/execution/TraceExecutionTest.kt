package execution

import org.jetbrains.kotlin.CallLogger
import org.jetbrains.kotlin.InstanceLogger
import org.jetbrains.kotlin.addCallLog.AddCallLogCommandLineProcessor
import org.jetbrains.kotlin.addCallLog.AddCallLogCommandLineProcessor.Companion.ENABLE_CALLS_TRACING
import org.jetbrains.kotlin.addCallLog.AddCallLogCommandLineProcessor.Companion.ENABLE_CLASS_TRACING
import org.jetbrains.kotlin.addCallLog.AddCallLogCommandLineProcessor.Companion.TRACE_CLASS
import org.jetbrains.kotlin.addCallLog.AddCallLogPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import utils.*
import utils.reflection.call
import utils.reflection.getCompanionProperty
import utils.reflection.newAnyInstance
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCompilerApi::class)
class TraceExecutionTest {

    @Rule
    @JvmField
    var tempDir = TemporaryFolder()

    @Test
    fun `test - excluded getters`() {
        val source = """
            class Database {
                fun getData(id: String): Data = Data(id)
            }
            
            class Cache {
                val cache = mutableMapOf<String, Data>()
            }
            
            class ApiService {
            
                val database = Database()
                val cache = Cache()
            
                fun getData(): Data {
                    return loadData("42")
                }
            
                private fun loadData(id: String): Data {
                    if (cache.cache.containsKey(id)) {
                        return cache.cache[id]!!
                    } else {
                        val data = database.getData(id)
                        cache.cache[id] = data
                        return data
                    }
                }
            }
            
            data class Data(val id: String = "42")
        """.trimIndent()

        val result = compile(
            sourceInfo = buildSourceInfo(tempDir, source),
            registrar = AddCallLogPluginRegistrar(),
            processors = mapOf(
                AddCallLogCommandLineProcessor() to listOf(
                    ENABLE_CALLS_TRACING.option to true
                )
            ),
        ).also { result -> result.assertSuccess() }

        val loader = result.classLoader
        val tracer =
            loader
                .loadClass(CallLogger::class.java)
                .kotlin.getCompanionProperty<CallLogger>("instance")

        val foo = loader.loadClass("ApiService").kotlin.newAnyInstance()

        tracer.stacks.assertEmpty()

        foo.call("getData")

        val children = tracer.threads.values.first().root?.children?.first()?.children ?: error("No children found")
        assertEquals(1, children.size)
    }

    @Test
    fun `test - basic tracing`() {
        val source = """
            class Foo {
                fun bar() = Unit
            }
            class Bar {
                fun foo() = Unit
            }
        """.trimIndent()

        val result = compile(
            sourceInfo = buildSourceInfo(tempDir, source),
            registrar = AddCallLogPluginRegistrar(),
            processors = mapOf(
                AddCallLogCommandLineProcessor() to listOf(
                    TRACE_CLASS.option to "Foo",
                    ENABLE_CLASS_TRACING.option to true
                )
            ),
        ).also { result -> result.assertSuccess() }

        val loader = result.classLoader
        val tracer =
            loader
                .loadClass(InstanceLogger::class.java)
                .kotlin.getCompanionProperty<InstanceLogger>("instance")

        val foo = loader.loadClass("Foo").kotlin.newAnyInstance()
        val bar = loader.loadClass("Bar").kotlin.newAnyInstance()

        tracer.getLog().assertEmpty()

        fun verifySkippedClass() {
            bar.call("foo")
            tracer.getLog().assertEmpty()
        }

        fun verifyTracedMethod() {
            foo.call("bar")

            assertEquals(1, tracer.getLog().size)

            tracer.getLog().forEach { log ->
                val traces = log.value

                assertEquals("Foo", log.key)
                assertEquals(1, traces.size)

                val trace = traces.first()


                assertEquals("bar", trace.methodName)
                assertNull(trace.returnValue)
                trace.params.assertEmpty()
            }
        }

        verifySkippedClass()
        verifyTracedMethod()
    }
}