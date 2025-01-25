import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.overrideMethod.OverrideMethodCommandLineProcessor
import org.jetbrains.kotlin.overrideMethod.OverrideMethodPluginRegistrar
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import utils.*
import kotlin.test.assertEquals


@OptIn(ExperimentalCompilerApi::class)
class OverrideMethodPluginTest {

    @Rule
    @JvmField
    var tempDir = TemporaryFolder()

    @Test
    fun `test - no changes when methods are already overridden`() {
        val source = """
            open class Parent {
                open fun foo() {}
                open fun bar() {}
            }

            class Child : Parent() {
                override fun foo() {}
                override fun bar() {}
            }
        """.trimIndent()

        val expected = """
            public final class Child
            extends Parent {
                @Override
                public void foo() {
                }
                @Override
                public void bar() {
                }
            }
        """.trimIndent()

        val result = compile(
            sourceInfo = buildSourceInfo(tempDir, source),
            registrar = OverrideMethodPluginRegistrar(),
            processor = OverrideMethodCommandLineProcessor(),
            options = { emptyList() }
        )

        result.assertSuccess()
        assertEquals(expected, result.decompileClassAndTrim("Child.class"))
    }

    @Test
    fun `test - add missing override methods`() {
        val source = """
            open class Parent {
                open fun foo() {}
                open fun bar() {}
            }

            class Child : Parent() {            
                override fun bar() {}
            }
        """.trimIndent()

        val expected = """
            public final class Child
            extends Parent {
                @Override
                public void bar() {
                }
                @Override
                public final void foo() {
                    System.out.println((Object)"hello");
                }
            }
        """.trimIndent()

        val result = compile(
            sourceInfo = buildSourceInfo(tempDir, source),
            registrar = OverrideMethodPluginRegistrar(),
            processor = OverrideMethodCommandLineProcessor(),
            options = { emptyList() }
        )

        result.assertSuccess()
        assertEquals(expected, result.decompileClassAndTrim("Child.class"))
    }
}