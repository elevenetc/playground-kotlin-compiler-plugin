@file:OptIn(UnsafeDuringIrConstructionAPI::class, ObsoleteDescriptorBasedAPI::class)

package org.jetbrains.kotlin.overrideMethod

import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.fir.scopes.impl.overrides
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.builders.declarations.addFunction
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.isSubtypeOfClass
import org.jetbrains.kotlin.ir.util.addChild
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.dumpKotlinLike
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.utils.addPrintlnBody

@OptIn(UnsafeDuringIrConstructionAPI::class)
class OverrideMethodTransformer(
    private val pluginContext: IrPluginContext
) : IrElementTransformerVoidWithContext(), FileLoweringPass {

    private val methodNameToOverride = Name.identifier("foo")

    override fun lower(irFile: IrFile) {
        irFile.transformChildrenVoid()
    }

    override fun visitClassNew(declaration: IrClass): IrStatement {
        if (declaration.hasSuperType) {
            val targetFunction = declaration.getSuperFunctionToOverride(methodNameToOverride)
            if (targetFunction != null) {

                if (declaration.definedFunctions.map { it.overridesFunction(targetFunction) }.filter { it }.isEmpty()) {
                    addOverrideFunction(declaration, targetFunction)
                }
            }
        }
        return super.visitClassNew(declaration)
    }

    private fun addOverrideFunction(irClass: IrClass, superFunction: IrSimpleFunction) {

        val fakeOverride = irClass.functions.firstOrNull {
            it.name == superFunction.name && it.origin == IrDeclarationOrigin.FAKE_OVERRIDE
        }

        if (fakeOverride != null) irClass.declarations.remove(fakeOverride)

        val irFunction = pluginContext.irFactory.buildFun {
            this.name = superFunction.name
            this.returnType = superFunction.returnType
        }.apply {
            dispatchReceiverParameter = irClass.thisReceiver
            addPrintlnBody("hello", this, pluginContext)
        }

        irClass.addChild(irFunction)
    }
}

private val IrClass.definedFunctions: List<IrSimpleFunction>
    get() {
        return functions.toList()
            .filter { it.origin == IrDeclarationOrigin.DEFINED && it !is IrConstructor }
    }

private val IrClass.hasSuperType: Boolean
    get() {
        return superTypes.any {
            it.isSubtypeOfClass(this.superClass()!!)
        }
    }

private fun IrClass.superClass(): IrClassSymbol? {
    return this.superTypes.mapNotNull { it.classOrNull }.firstOrNull { it.owner.kind == ClassKind.CLASS }
}

private fun IrClass.getSuperFunctionToOverride(methodName: Name): IrSimpleFunction? {
    return this.superClass()?.owner?.functions?.firstOrNull { it.name == methodName }
}

private fun IrSimpleFunction.overridesFunction(targetFunction: IrSimpleFunction): Boolean {
    return overriddenSymbols.any { it.owner == targetFunction && targetFunction.name == this.name }
}