package org.jetbrains.kotlin.addProperty

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

class AddPropertyPluginExtension(
    private val propertyName: String,
    private val propertyType: String,
    private val propertyValue: String
) : IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        moduleFragment.transform(
            AddPropertyTransformer(
                propertyName,
                propertyType,
                propertyValue,
                pluginContext
            ), null
        )
    }
}