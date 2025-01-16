import org.jetbrains.kotlin.cli.common.ExitCode
import org.junit.After
import org.junit.Test
import utils.compile
import utils.generateSourceFile
import java.io.File
import kotlin.test.assertEquals

class BasicCompilationTest {

    private var sourcePath = ""

    @Test
    fun `test - successful compilation`() {
        sourcePath = generateSourceFile("val a = 10", "success.kt")
        val result = compile(sourcePath)
        assertEquals(ExitCode.OK, result)
    }

    @Test
    fun `test - failed compilation`() {
        sourcePath = generateSourceFile("val a = b", "fail.kt")
        val result = compile(sourcePath)
        assertEquals(ExitCode.COMPILATION_ERROR, result)
    }

    @After
    fun cleanup() {
        File(sourcePath).delete()
    }
}