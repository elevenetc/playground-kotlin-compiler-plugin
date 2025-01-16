import org.jetbrains.kotlin.addDependencyCallToMethod.AddDependencyCallToMethodCommandLineProcessor
import org.jetbrains.kotlin.addDependencyCallToMethod.AddDependencyCallToMethodPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import utils.assertSuccess
import utils.buildSourceInfo
import utils.compile
import utils.decompileClassAndTrim
import utils.reflection.call
import utils.reflection.get
import utils.reflection.getCompanionProperty
import utils.reflection.newInstance
import java.io.File
import kotlin.test.assertEquals

@OptIn(ExperimentalCompilerApi::class)
class AddDependencyCallToMethodPluginTest {

    @Rule
    @JvmField
    var tempDir = TemporaryFolder()

    @Test
    fun `test - static instance of IntValueHolder injectected and bar() updates int value`() {

        val intValueHolderSource = """
            class IntValueHolder {
            
                var value: Int = 0
            
                fun incrementAndGetValue(): Int {
                    return ++value
                }
            
                companion object {
                    @JvmStatic
                    val instance = IntValueHolder()
                }
            }
        """.trimIndent()

        val fooSource = """
            class Foo {
             fun bar() {
                 
             }
            }
        """.trimIndent()

        compile(buildSourceInfo(tempDir, intValueHolderSource)).assertSuccess()

        val result = compile(
            sourceInfo = buildSourceInfo(tempDir, fooSource),
            registrar = AddDependencyCallToMethodPluginRegistrar(),
            processor = AddDependencyCallToMethodCommandLineProcessor(),
            classpath = listOf(File(tempDir.root, "classes"))
        ).also { result -> result.assertSuccess() }

        assertEquals(
            4,
            result.compiledClassAndResourceFiles.size,
            message = "Not all classes compiled: " + result.compiledClassAndResourceFiles.map { it.name }
        )

        val loader = result.classLoader
        val foo = loader.loadClass("Foo").kotlin.newInstance()
        val invValueHolder = loader.loadClass("IntValueHolder").kotlin.getCompanionProperty("instance")

        assertEquals(0, invValueHolder.get("value"))
        foo.call("bar")
        assertEquals(1, invValueHolder.get("value"))
        foo.call("bar")
        assertEquals(2, invValueHolder.get("value"))


        val expected = """
            public final class Foo {
                public final void bar() {
                    IntValueHolder.Companion.getInstance().incrementAndGetValue();
                }
            }
        """.trimIndent()

        assertEquals(expected, result.decompileClassAndTrim("Foo.class"))
    }
}