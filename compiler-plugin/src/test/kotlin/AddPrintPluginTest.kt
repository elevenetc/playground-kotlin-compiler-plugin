import org.jetbrains.kotlin.addPrint.AddPrintCommandLineProcessor
import org.jetbrains.kotlin.addPrint.AddPrintCommandLineProcessor.Companion.STRING_VALUE_OPTION
import org.jetbrains.kotlin.addPrint.AddPrintPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import utils.assertSuccess
import utils.buildSourceInfo
import utils.compile
import utils.decompileClassAndTrim
import kotlin.test.assertEquals

@OptIn(ExperimentalCompilerApi::class)
class AddPrintPluginTest {

    @Rule
    @JvmField
    var tempDir = TemporaryFolder()

    @Test
    fun `test - add println to all methods of class`() {

        val stringValue = "hello"

        val source = """
          class Foo {
            fun bar1() {
                
            }
            fun bar2() {
                
            }
          }
        """.trimIndent()

        val expected = """
            public final class Foo {
                public final void bar1() {
                    System.out.println((Object)"$stringValue");
                }
                public final void bar2() {
                    System.out.println((Object)"$stringValue");
                }
            }
        """.trimIndent()

        val result = compile(
            sourceInfo = buildSourceInfo(tempDir, source),
            registrar = AddPrintPluginRegistrar(),

            processors = mapOf(
                AddPrintCommandLineProcessor() to listOf(
                    STRING_VALUE_OPTION to stringValue
                )
            )
        )

        result.assertSuccess()
        assertEquals(expected, result.decompileClassAndTrim())
    }
}