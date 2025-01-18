package org.jetbrains.kotlin.addMethodArgument

import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey
import org.jetbrains.kotlin.utils.Option

@OptIn(ExperimentalCompilerApi::class)
class AddMethodArgumentCommandLineProcessor : CommandLineProcessor {

    companion object {
        const val METHOD_NAME = "methodName"
        const val ARGUMENT_NAME = "argumentName"
        const val ARGUMENT_TYPE = "argumentType"

        val METHOD_NAME_OPTION = Option(METHOD_NAME, "Name of method where argument needs to be injected")
        val ARGUMENT_NAME_OPTION = Option(ARGUMENT_NAME, "Name of injected argument")
        val ARGUMENT_TYPE_OPTION = Option(ARGUMENT_TYPE, "Type of injected argument")

        internal val METHOD_NAME_OPTION_KEY = CompilerConfigurationKey<String>(METHOD_NAME_OPTION.description)
        internal val ARGUMENT_NAME_OPTION_KEY = CompilerConfigurationKey<String>(ARGUMENT_NAME_OPTION.description)
        internal val ARGUMENT_TYPE_OPTION_KEY = CompilerConfigurationKey<String>(ARGUMENT_TYPE_OPTION.description)
    }

    override val pluginId: String = "add.method.argument"
    override val pluginOptions: Collection<AbstractCliOption> = listOf(
        METHOD_NAME_OPTION, ARGUMENT_NAME_OPTION, ARGUMENT_TYPE_OPTION
    )

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
        when (option.optionName) {
            METHOD_NAME -> configuration.put(METHOD_NAME_OPTION_KEY, value)
            ARGUMENT_NAME -> configuration.put(ARGUMENT_NAME_OPTION_KEY, value)
            ARGUMENT_TYPE -> configuration.put(ARGUMENT_TYPE_OPTION_KEY, value)
            else -> error("Unknown plugin option: ${option.optionName}")
        }
    }
}

