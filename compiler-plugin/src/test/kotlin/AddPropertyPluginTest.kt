import addProperty.AddPropertyCommandLineProcessor
import addProperty.AddPropertyPluginRegistrar
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import utils.*
import kotlin.test.assertEquals

@OptIn(ExperimentalCompilerApi::class)
class AddPropertyPluginTest {

    @Rule
    @JvmField
    var tempDir = TemporaryFolder()

    @Test
    fun `test - int property injection`() {

        val propertyName = "intProp"
        val propertyType = "Int"
        val propertyValue = "42"

        @Language("java")
        val expected = """
            public final class Foo {
                public final int $propertyName;
                public Foo() {
                    this.$propertyName = $propertyValue;
                }
            }
        """.trimIndent()

        doTest(expected, propertyName, propertyType, propertyValue)
    }

    @Test
    fun `test - String property injection`() {

        val propertyName = "stringProp"
        val propertyType = "String"
        val propertyValue = "abc"

        @Language("java")
        val expected = """
            import org.jetbrains.annotations.NotNull;
            public final class Foo {
                @NotNull
                public final $propertyType $propertyName;
                public Foo() {
                    this.$propertyName = "$propertyValue";
                }
            }
        """.trimIndent()

        doTest(expected, propertyName, propertyType, propertyValue)
    }

    private fun doTest(
        expected: String,
        propertyName: String,
        propertyType: String,
        propertyValue: String,
    ) {

        val propertyFqnType = "kotlin.$propertyType"

        val source = """
          class Foo {
            
          }
        """.trimIndent()

        val result = compile(
            sourceInfo = buildSourceInfo(tempDir, source),
            registrar = AddPropertyPluginRegistrar(),
            processor = AddPropertyCommandLineProcessor(),
            options = {
                listOf(
                    option(AddPropertyCommandLineProcessor.PROPERTY_NAME_OPTION, propertyName),
                    option(AddPropertyCommandLineProcessor.PROPERTY_TYPE_OPTION, propertyFqnType),
                    option(AddPropertyCommandLineProcessor.PROPERTY_VALUE_OPTION, propertyValue)
                )
            }
        )

        result.assertSuccess()
        val actual = result.decompileClassAndTrim()
        assertEquals(expected, actual)
    }
}