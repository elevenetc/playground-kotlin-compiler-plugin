package execution

import org.jetbrains.kotlin.InstanceLogger
import org.jetbrains.kotlin.addCallLog.AddCallLogCommandLineProcessor
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