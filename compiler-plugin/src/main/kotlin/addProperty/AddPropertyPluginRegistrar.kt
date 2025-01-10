package addProperty

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration

@OptIn(ExperimentalCompilerApi::class)
class AddPropertyPluginRegistrar : CompilerPluginRegistrar() {
    override val supportsK2: Boolean
        get() = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {

        val propertyName = checkNotNull(configuration[AddPropertyCommandLineProcessor.PROPERTY_NAME_OPTION_KEY])
        val propertyType = checkNotNull(configuration[AddPropertyCommandLineProcessor.PROPERTY_TYPE_OPTION_KEY])
        val propertyValue = checkNotNull(configuration[AddPropertyCommandLineProcessor.PROPERTY_VALUE_OPTION_KEY])

        IrGenerationExtension.registerExtension(
            AddPropertyPluginExtension(
                propertyName,
                propertyType,
                propertyValue
            )
        )
    }
}