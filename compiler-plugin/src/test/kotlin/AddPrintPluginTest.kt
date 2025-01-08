import addPrint.AddPrintCommandLineProcessor
import addPrint.AddPrintPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import utils.*
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
            processor = AddPrintCommandLineProcessor(),
            options = {
                listOf(option(AddPrintCommandLineProcessor.STRING_VALUE_OPTION, stringValue))
            },
        )

        result.assertSuccess()
        assertEquals(expected, result.decompileClassAndTrim())
    }
}