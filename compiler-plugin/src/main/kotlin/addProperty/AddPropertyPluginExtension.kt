package addProperty

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.impl.IrFactoryImpl

class AddPropertyPluginExtension(
    private val propertyName: String,
    private val propertyType: String,
    private val propertyValue: String,
    private val irFactory: IrFactoryImpl
) : IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        val s: String
        moduleFragment.transform(
            AddPropertyTransformer(
                propertyName,
                propertyType,
                propertyValue,
                irFactory,
                pluginContext
            ), null
        )
    }
}