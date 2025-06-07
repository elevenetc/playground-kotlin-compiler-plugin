import org.jetbrains.kotlin.addMethodArgument.AddMethodArgumentCommandLineProcessor
import org.jetbrains.kotlin.addMethodArgument.AddMethodArgumentPluginRegistrar
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
class AddMethodArgumentPluginTest {

    @Rule
    @JvmField
    var tempDir = TemporaryFolder()

    @Test
    fun `test - add int argument to method`() {

        val argumentName = "intArg"
        val argumentType = "Int"

        val expected = """
            public final class Foo {
                public final void bar(int intArg) {
                }
            }
        """.trimIndent()

        doTest(argumentName, argumentType, sourceWithNoParameters, expected)
    }

    @Test
    fun `test - add String argument to method`() {

        val argumentName = "stringArg"
        val argumentType = "String"

        val expected = """
            public final class Foo {
                public final void bar(@NotNull String stringArg) {
                    Intrinsics.checkNotNullParameter(stringArg, "stringArg");
                }
            }
        """.trimIndent()

        doTest(argumentName, argumentType, sourceWithNoParameters, expected)
    }

    @Test
    fun `test - new argument doesn't override existing`() {

        val argumentName = "intArg"
        val argumentType = "Int"

        val expected = """
            public final class Foo {
                public final void bar(int prop, int intArg) {
                }
            }
        """.trimIndent()

        doTest(argumentName, argumentType, sourceWithParameter, expected)
    }

    @Test
    fun `test - ignored method must not be modified`() {

        val argumentName = "intArg"
        val argumentType = "Int"

        val expected = """
            public final class Foo {
                public final void bar(int intArg) {
                }
                public final void mustBeIgnored() {
                }
            }
        """.trimIndent()

        doTest(argumentName, argumentType, sourceWithNoParametersAndIgnoredMethod, expected)
    }

    private fun doTest(
        argumentName: String,
        argumentType: String,
        source: String,
        expected: String
    ) {

        val fqnType = "kotlin.$argumentType"

        val result = compile(
            sourceInfo = buildSourceInfo(tempDir, source),
            registrar = AddMethodArgumentPluginRegistrar(),

            processors = mapOf(
                AddMethodArgumentCommandLineProcessor() to listOf(
                    AddMethodArgumentCommandLineProcessor.METHOD_NAME_OPTION to "bar",
                    AddMethodArgumentCommandLineProcessor.ARGUMENT_NAME_OPTION to argumentName,
                    AddMethodArgumentCommandLineProcessor.ARGUMENT_TYPE_OPTION to fqnType
                )
            )
        )

        result.assertSuccess()
        val actual = result.decompileClassAndTrim()
        assertEquals(expected, actual)
    }
}

private val sourceWithNoParameters = """
          class Foo {
              fun bar() {}
          }
        """.trimIndent()

private val sourceWithNoParametersAndIgnoredMethod = """
          class Foo {
              fun bar() {}
              fun mustBeIgnored() {}
          }
        """.trimIndent()

private val sourceWithParameter = """
          class Foo {
              fun bar(prop: Int) {}
          }
        """.trimIndent()