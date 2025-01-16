package utils

import com.tschuchort.compiletesting.JvmCompilationResult
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi

@OptIn(ExperimentalCompilerApi::class)
fun JvmCompilationResult.printCompiledClass(fileName: String) {
    printlnCode(decompileClassAndTrim(fileName), fileName)
}