package org.jetbrains.kotlin.addProperty

import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey
import org.jetbrains.kotlin.utils.Option

@OptIn(ExperimentalCompilerApi::class)
class AddPropertyCommandLineProcessor : CommandLineProcessor {

    companion object {
        const val PROPERTY_NAME = "propertyName"
        const val PROPERTY_TYPE = "propertyType"
        const val PROPERTY_VALUE = "propertyValue"

        val PROPERTY_NAME_OPTION = Option(name = PROPERTY_NAME, description = "Name of injected property")
        val PROPERTY_TYPE_OPTION = Option(name = PROPERTY_TYPE, description = "Type of injected property")
        val PROPERTY_VALUE_OPTION = Option(name = PROPERTY_VALUE, description = "Value of injected property")

        internal val PROPERTY_NAME_OPTION_KEY = CompilerConfigurationKey<String>(PROPERTY_NAME_OPTION.description)
        internal val PROPERTY_TYPE_OPTION_KEY = CompilerConfigurationKey<String>(PROPERTY_TYPE_OPTION.description)
        internal val PROPERTY_VALUE_OPTION_KEY = CompilerConfigurationKey<String>(PROPERTY_VALUE_OPTION.description)
    }

    override val pluginId: String = "add.property"
    override val pluginOptions: Collection<AbstractCliOption> = listOf(
        PROPERTY_NAME_OPTION,
        PROPERTY_TYPE_OPTION,
        PROPERTY_VALUE_OPTION
    )

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
        when (option.optionName) {
            PROPERTY_NAME -> configuration.put(PROPERTY_NAME_OPTION_KEY, value)
            PROPERTY_TYPE -> configuration.put(PROPERTY_TYPE_OPTION_KEY, value)
            PROPERTY_VALUE -> configuration.put(PROPERTY_VALUE_OPTION_KEY, value)
            else -> error("Unknown plugin option: ${option.optionName}")
        }
    }
}