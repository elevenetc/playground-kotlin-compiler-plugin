package org.jetbrains.kotlin.addPrint

import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

@OptIn(ExperimentalCompilerApi::class)
class AddPrintCommandLineProcessor : CommandLineProcessor {

    companion object {
        const val STRING_VALUE = "stringValue"

        val STRING_VALUE_OPTION = CliOption(
            optionName = STRING_VALUE,
            valueDescription = "String",
            description = "The string which should be printed",
            required = true,
            allowMultipleOccurrences = false,
        )

        internal val STRING_VALUE_KEY = CompilerConfigurationKey<String>(STRING_VALUE_OPTION.description)
    }

    override val pluginId: String = "add.print"
    override val pluginOptions: Collection<AbstractCliOption> = listOf(STRING_VALUE_OPTION)

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
        when (option.optionName) {
            STRING_VALUE -> configuration.put(STRING_VALUE_KEY, value)
            else -> error("Unknown plugin option: ${option.optionName}")
        }
    }
}

