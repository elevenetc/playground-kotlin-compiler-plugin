package org.jetbrains.kotlin.addMethod

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

class AddMethodPluginExtension(
    private val methodName: String,
    private val isStatic: Boolean
) : IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        moduleFragment.transform(
            AddMethodTransformer(
                methodName,
                isStatic,
                pluginContext
            ), null
        )
    }
}