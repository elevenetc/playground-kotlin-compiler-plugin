package addPrint

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration

@OptIn(ExperimentalCompilerApi::class)
class AddPrintPluginRegistrar : CompilerPluginRegistrar() {
    override val supportsK2: Boolean
        get() = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {

        val stringValue = checkNotNull(configuration[AddPrintCommandLineProcessor.STRING_VALUE_KEY])

        IrGenerationExtension.registerExtension(
            AddPrintPluginExtension(stringValue)
        )
    }
}