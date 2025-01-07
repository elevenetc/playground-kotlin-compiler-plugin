package utils

import com.tschuchort.compiletesting.SourceFile
import org.intellij.lang.annotations.Language
import org.junit.rules.TemporaryFolder

data class SourceInfo(
    val tempDir: TemporaryFolder,
) {
    internal val sourceFiles = mutableListOf<SourceFile>()

    fun addSources(vararg sourceFiles: SourceFile): SourceInfo {
        this.sourceFiles.addAll(sourceFiles)
        return this
    }
}

fun buildSourceInfo(tempDir: TemporaryFolder, @Language("kotlin") source: String): SourceInfo {
    return buildSourceInfo(tempDir).addSources(SourceFile.kotlin("source.kt", source))
}

fun buildSourceInfo(tempDir: TemporaryFolder): SourceInfo {
    return SourceInfo(tempDir)
}