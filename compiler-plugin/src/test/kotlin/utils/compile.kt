package utils

import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.PluginOption
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.Services


@OptIn(ExperimentalCompilerApi::class)
internal fun compile(
    sourceInfo: SourceInfo,
    registrar: CompilerPluginRegistrar,
    processor: CommandLineProcessor,
    options: CommandLineProcessor.() -> List<PluginOption>
): JvmCompilationResult {
    return prepareCompilation(
        sourceInfo,
        registrar,
        processor,
        options
    ).compile()
}

fun compile(
    path: String
): ExitCode {
    return compile(listOf(path))
}

fun compile(
    filePaths: List<String>
): ExitCode {
    val args = K2JVMCompilerArguments().apply {
        enableDebugMode = true
        freeArgs = filePaths
        noStdlib = true
        noReflect = true
        includeRuntime = false
        moduleName = "testModule"
        verbose = true
        kotlinHome = null
        script = false
        version = true
        disableStandardScript = true
        disableDefaultScriptingPlugin = true
        destination = "build/classes/kotlin/main"
    }
    val messageCollector = PrintingMessageCollector(System.err, MessageRenderer.GRADLE_STYLE, true)
    return K2JVMCompiler().exec(messageCollector, Services.EMPTY, args)
}

@OptIn(ExperimentalCompilerApi::class)
internal fun prepareCompilation(
    sourceInfo: SourceInfo,
    registrar: CompilerPluginRegistrar,
    processor: CommandLineProcessor,
    options: CommandLineProcessor.() -> List<PluginOption>
): KotlinCompilation {
    return KotlinCompilation().apply {
        workingDir = sourceInfo.tempDir.root
        compilerPluginRegistrars = listOf(registrar)
        commandLineProcessors = listOf(processor)
        pluginOptions = options.invoke(processor)
        inheritClassPath = true
        sources = sourceInfo.sourceFiles.toList()
        verbose = true
    }
}