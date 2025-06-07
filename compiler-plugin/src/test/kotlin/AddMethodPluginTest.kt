import org.jetbrains.kotlin.addMethod.AddMethodCommandLineProcessor
import org.jetbrains.kotlin.addMethod.AddMethodCommandLineProcessor.Companion.IS_STATIC_OPTION
import org.jetbrains.kotlin.addMethod.AddMethodCommandLineProcessor.Companion.NAME_OPTION
import org.jetbrains.kotlin.addMethod.AddMethodPluginRegistrar
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
class AddMethodPluginTest {

    @Rule
    @JvmField
    var tempDir = TemporaryFolder()

    @Test
    fun `test - instance method injection`() {

        val methodName = "helloInstanceWorld"

        val expected = """
            public final class Foo {
                public final void $methodName() {
                    System.out.println((Object)"$methodName");
                }
            }
        """.trimIndent()

        doTest(expected, methodName, false)
    }

    @Test
    fun `test - static method injection`() {

        val methodName = "helloStaticWorld"

        val expected = """
            public final class Foo {
                public static final void $methodName() {
                    System.out.println((Object)"$methodName");
                }
            }
        """.trimIndent()

        doTest(expected, methodName, true)
    }

    private fun doTest(
        expected: String,
        methodName: String,
        isStatic: Boolean,
    ) {
        val source = """
                    class Foo           
        """.trimIndent()

        val result = compile(
            sourceInfo = buildSourceInfo(tempDir, source),
            registrar = AddMethodPluginRegistrar(),

            processors = mapOf(
                AddMethodCommandLineProcessor() to listOf(
                    NAME_OPTION to methodName,
                    IS_STATIC_OPTION to isStatic
                )
            )
        )

        result.assertSuccess()
        assertEquals(expected, result.decompileClassAndTrim())
    }
}