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
        val ENABLE = CmdOption(
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

        val EXCLUDED_FQN = CmdOption(
            "excludedFqn",
            CliOption(
                optionName = "excludedFqn",
                valueDescription = "String",
                description = "Excluded fqn",
                required = false,
                allowMultipleOccurrences = true,
            ),
            CompilerConfigurationKey<String>("excluded fqn")
        )

        val EXCLUDED_FILES = CmdOption(
            "excludedFiles",
            CliOption(
                optionName = "excludedFiles",
                valueDescription = "String",
                description = "Excluded files",
                required = false,
                allowMultipleOccurrences = true,
            ),
            CompilerConfigurationKey<String>("excluded files")
        )
    }

    override val pluginId: String = "playground.compiler.plugin.compiler"
    override val pluginOptions: Collection<AbstractCliOption> = listOf(
        ENABLE.option,
        EXCLUDED_FQN.option,
        EXCLUDED_FILES.option
    )

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
        when (option.optionName) {
            ENABLE.value -> configuration.put(ENABLE.key, value.toBoolean())
            EXCLUDED_FQN.value -> {
                val existing = configuration.get(EXCLUDED_FQN.key)
                if (existing != null) configuration.put(EXCLUDED_FQN.key, "$existing|$value")
                else configuration.put(EXCLUDED_FQN.key, value)
            }

            EXCLUDED_FILES.value -> {
                val existing = configuration.get(EXCLUDED_FILES.key)
                if (existing != null) configuration.put(EXCLUDED_FILES.key, "$existing|$value")
                else configuration.put(EXCLUDED_FILES.key, value)
            }

            else -> error("Unknown plugin option: ${option.optionName}")
        }
    }
}

data class CmdOption<T>(
    val value: String,
    val option: CliOption,
    val key: CompilerConfigurationKey<T>
)