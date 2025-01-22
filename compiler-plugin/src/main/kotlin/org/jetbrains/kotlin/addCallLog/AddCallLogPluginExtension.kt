package org.jetbrains.kotlin.addCallLog

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

class AddCallLogPluginExtension(val excludedFqns: List<String>) : IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        moduleFragment.transform(AddCallLogTransformer(excludedFqns, pluginContext), null)
    }
}