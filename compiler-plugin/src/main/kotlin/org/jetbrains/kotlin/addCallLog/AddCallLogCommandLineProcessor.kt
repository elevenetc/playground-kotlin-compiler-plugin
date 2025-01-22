package org.jetbrains.kotlin.addCallLog

import com.google.auto.service.AutoService
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

@OptIn(ExperimentalCompilerApi::class)
@AutoService(CommandLineProcessor::class)
class AddCallLogCommandLineProcessor : CommandLineProcessor {

    companion object {
        val ENABLE = CmdOption<Boolean>(
            "enabledValue",
            CliOption(
                optionName = "enabledValue",
                valueDescription = "Boolean",
                description = "Enable plugin",
                required = false,
                allowMultipleOccurrences = false,
            ),
            CompilerConfigurationKey<Boolean>("Enable plugin")
        )

        val EXCLUDED_FQNS = CmdOption<List<String>>(
            "excludedFqns",
            CliOption(
                optionName = "excludedFqns",
                valueDescription = "List<String>",
                description = "Excluded fqns",
                required = false,
                allowMultipleOccurrences = false,
            ),
            CompilerConfigurationKey<List<String>>("Excluded fqns")
        )
    }

    override val pluginId: String = "playground.compiler.plugin.compiler"
    override val pluginOptions: Collection<AbstractCliOption> = listOf(ENABLE.option, EXCLUDED_FQNS.option)

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
        when (option.optionName) {
            ENABLE.value -> configuration.put(ENABLE.key, value.toBoolean())
            EXCLUDED_FQNS.value -> configuration.put(
                EXCLUDED_FQNS.key,
                value.toStringList()
            )

            else -> error("Unknown plugin option: ${option.optionName}")
        }
    }
}

private fun String.toStringList(): List<String> {
    return removeSurrounding("[", "]").split(',').map { it.trim() }
}

data class CmdOption<T>(
    val value: String,
    val option: CliOption,
    val key: CompilerConfigurationKey<T>
)