import org.gradle.testkit.runner.GradleRunner
import org.junit.Ignore
import org.junit.Test
import java.io.File
import kotlin.test.assertTrue

class GradlePluginTests {

    val projectDirPath = ""
    val pluginDirPath = ""

    @Ignore
    @Test
    fun `test - remote plugin`() {

        val projectPath = File(projectDirPath)
        val pluginPath = listOf(File(pluginDirPath))
        val runner = GradleRunner.create()
            .withProjectDir(projectPath)
            .withPluginClasspath(pluginPath)
            .withArguments("build -Dorg.gradle.debug=true --no-daemon")
            .forwardOutput()

        val result = runner.build()
        assertTrue(result.output.contains("Expected Output"))
    }
}