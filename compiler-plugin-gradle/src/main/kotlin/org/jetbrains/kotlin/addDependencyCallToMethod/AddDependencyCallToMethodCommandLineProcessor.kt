package org.jetbrains.kotlin.addDependencyCallToMethod

import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration

@OptIn(ExperimentalCompilerApi::class)
class AddDependencyCallToMethodCommandLineProcessor : CommandLineProcessor {
    override val pluginId: String = "add.dependency.to.method"
    override val pluginOptions: Collection<AbstractCliOption> = emptyList()

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
        when (option.optionName) {
            else -> error("Unknown plugin option: ${option.optionName}")
        }
    }
}

