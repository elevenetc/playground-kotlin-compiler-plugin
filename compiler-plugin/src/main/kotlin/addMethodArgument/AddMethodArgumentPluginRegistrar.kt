package addMethodArgument

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration

@OptIn(ExperimentalCompilerApi::class)
class AddMethodArgumentPluginRegistrar : CompilerPluginRegistrar() {
    override val supportsK2: Boolean
        get() = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {

        val methodName = checkNotNull(configuration[AddMethodArgumentCommandLineProcessor.METHOD_NAME_OPTION_KEY])
        val argumentName = checkNotNull(configuration[AddMethodArgumentCommandLineProcessor.ARGUMENT_NAME_OPTION_KEY])
        val argumentType = checkNotNull(configuration[AddMethodArgumentCommandLineProcessor.ARGUMENT_TYPE_OPTION_KEY])

        IrGenerationExtension.registerExtension(
            AddMethodArgumentPluginExtension(
                methodName,
                argumentName,
                argumentType
            )
        )
    }
}