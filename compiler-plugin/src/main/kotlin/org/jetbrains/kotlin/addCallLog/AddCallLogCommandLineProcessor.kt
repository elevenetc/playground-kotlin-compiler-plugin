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

        /**
         * Calls tracing
         */

        val ENABLE_CALLS_TRACING = CmdOption(
            "enableCallsTracing",
            CliOption(
                optionName = "enableCallsTracing",
                valueDescription = "Boolean",
                description = "Enable call tracing for all functions",
                required = false,
            ),
            CompilerConfigurationKey<Boolean>("Enable call tracing for all functions")
        )

        val EXCLUDED_FQN = CmdOption(
            "excludedCallsTracingFqns",
            CliOption(
                optionName = "excludedCallsTracingFqns",
                valueDescription = "String",
                description = "Excluded calls tracing fqns",
                required = false,
                allowMultipleOccurrences = true,
            ),
            CompilerConfigurationKey<String>("excluded tracing fqn")
        )

        val EXCLUDED_FILES = CmdOption(
            "excludedCallsTracingFiles",
            CliOption(
                optionName = "excludedCallsTracingFiles",
                valueDescription = "String",
                description = "Excluded files tracing fqns",
                required = false,
                allowMultipleOccurrences = true,
            ),
            CompilerConfigurationKey<String>("excluded tracing files")
        )

        /**
         * Class tracing
         */

        val ENABLE_CLASS_TRACING = CmdOption(
            "enableClassTracing",
            CliOption(
                optionName = "enableClassTracing",
                valueDescription = "Boolean",
                description = "Enable class tracing for classes listed in `traceClasses`",
                required = false
            ),
            CompilerConfigurationKey<Boolean>("Enable call tracing for all functions")
        )

        val TRACE_CLASS = CmdOption(
            "traceClasses",
            CliOption(
                optionName = "traceClasses",
                valueDescription = "String",
                description = "FQN of class to be traced",
                required = false,
                allowMultipleOccurrences = true,
            ),
            CompilerConfigurationKey<String>("trace class fqns, separated by ','")
        )
    }

    override val pluginId: String = "playground.compiler.plugin.compiler"

    override val pluginOptions: Collection<AbstractCliOption> = listOf(
        ENABLE_CALLS_TRACING.option,
        EXCLUDED_FQN.option,
        EXCLUDED_FILES.option,

        ENABLE_CLASS_TRACING.option,
        TRACE_CLASS.option,
    )

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
        when (option.optionName) {
            ENABLE_CALLS_TRACING.value -> configuration.put(ENABLE_CALLS_TRACING.key, value.toBoolean())
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


            ENABLE_CLASS_TRACING.value -> configuration.put(ENABLE_CLASS_TRACING.key, value.toBoolean())

            TRACE_CLASS.value -> {
                val existing = configuration.get(TRACE_CLASS.key)
                if (existing != null) configuration.put(TRACE_CLASS.key, "$existing|$value")
                else configuration.put(TRACE_CLASS.key, value)
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