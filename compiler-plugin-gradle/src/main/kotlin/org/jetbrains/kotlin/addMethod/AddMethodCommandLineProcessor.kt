package org.jetbrains.kotlin.addMethod

import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey
import org.jetbrains.kotlin.utils.Option
import java.lang.Boolean.parseBoolean

@OptIn(ExperimentalCompilerApi::class)
class AddMethodCommandLineProcessor : CommandLineProcessor {

    companion object {
        const val NAME = "methodName"
        const val IS_STATIC = "isStatic"

        val NAME_OPTION = Option(name = NAME, description = "Name of injected method")
        val IS_STATIC_OPTION = Option(name = IS_STATIC, description = "Inject static or instance method")

        internal val NAME_OPTION_KEY = CompilerConfigurationKey<String>(NAME_OPTION.description)
        internal val IS_STATIC_OPTION_KEY = CompilerConfigurationKey<Boolean>(IS_STATIC_OPTION.description)
    }

    override val pluginId: String = "add.method"
    override val pluginOptions: Collection<AbstractCliOption> = listOf(
        NAME_OPTION,
        IS_STATIC_OPTION,
    )

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
        when (option.optionName) {
            NAME -> configuration.put(NAME_OPTION_KEY, value)
            IS_STATIC -> configuration.put(IS_STATIC_OPTION_KEY, parseBoolean(value))
            else -> error("Unknown plugin option: ${option.optionName}")
        }
    }
}

