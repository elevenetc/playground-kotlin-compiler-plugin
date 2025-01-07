package utils

import com.tschuchort.compiletesting.PluginOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi

@OptIn(ExperimentalCompilerApi::class)
internal fun CommandLineProcessor.option(key: CliOption, value: Any?): PluginOption {
    return PluginOption(pluginId, key.optionName, value.toString())
}