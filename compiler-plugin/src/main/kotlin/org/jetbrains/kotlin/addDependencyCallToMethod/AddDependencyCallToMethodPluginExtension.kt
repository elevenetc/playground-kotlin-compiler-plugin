package org.jetbrains.kotlin.addDependencyCallToMethod

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

class AddDependencyCallToMethodPluginExtension : IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        moduleFragment.transform(AddDependencyCallToMethodTransformer(pluginContext), null)
    }
}