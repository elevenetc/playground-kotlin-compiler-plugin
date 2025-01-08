package addMethod

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.impl.IrFactoryImpl

class AddMethodPluginExtension(
    private val methodName: String,
    private val isStatic: Boolean,
    private val irFactory: IrFactoryImpl
) : IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        moduleFragment.transform(
            AddMethodTransformer(
                methodName,
                isStatic,
                irFactory,
                pluginContext
            ), null
        )
    }
}