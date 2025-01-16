package org.jetbrains.kotlin.addMethodArgument

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

class AddMethodArgumentPluginExtension(
    private val methodName: String,
    private val argumentName: String,
    private val argumentType: String
) : IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        moduleFragment.transform(
            AddMethodArgumentTransformer(
                methodName,
                argumentName,
                argumentType,
                pluginContext
            ), null
        )
    }
}