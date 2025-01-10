package addMethod

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.ir.declarations.impl.IrFactoryImpl

@OptIn(ExperimentalCompilerApi::class)
class AddMethodPluginRegistrar : CompilerPluginRegistrar() {
    override val supportsK2: Boolean
        get() = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {

        val methodName = checkNotNull(configuration[AddMethodCommandLineProcessor.NAME_OPTION_KEY])
        val isStatic = checkNotNull(configuration[AddMethodCommandLineProcessor.IS_STATIC_OPTION_KEY])

        IrGenerationExtension.registerExtension(
            AddMethodPluginExtension(
                methodName,
                isStatic
            )
        )
    }
}