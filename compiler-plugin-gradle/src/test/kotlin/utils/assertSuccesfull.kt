package utils

import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import kotlin.test.assertEquals

@OptIn(ExperimentalCompilerApi::class)
fun JvmCompilationResult.assertSuccess() {
    assertEquals(KotlinCompilation.ExitCode.OK, exitCode)
}