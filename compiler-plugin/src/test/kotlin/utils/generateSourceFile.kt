package utils

import org.intellij.lang.annotations.Language
import java.nio.file.Files
import java.nio.file.Paths

fun generateSourceFile(@Language("kotlin") source: String, fileName: String = "test.kt"): String {
    val resourcesPath = Paths.get("build", "generated")
    Files.createDirectories(resourcesPath)
    val filePath = resourcesPath.resolve(fileName)
    Files.write(filePath, source.toByteArray())
    return filePath.toAbsolutePath().toString()
}