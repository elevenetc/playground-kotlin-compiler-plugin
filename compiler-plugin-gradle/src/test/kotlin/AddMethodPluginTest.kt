import org.jetbrains.kotlin.addMethod.AddMethodCommandLineProcessor
import org.jetbrains.kotlin.addMethod.AddMethodPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import utils.*
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
            processor = AddMethodCommandLineProcessor(),
            options = {
                listOf(
                    option(AddMethodCommandLineProcessor.NAME_OPTION, methodName),
                    option(AddMethodCommandLineProcessor.IS_STATIC_OPTION, isStatic),
                )
            }
        )

        result.assertSuccess()
        assertEquals(expected, result.decompileClassAndTrim())
    }
}