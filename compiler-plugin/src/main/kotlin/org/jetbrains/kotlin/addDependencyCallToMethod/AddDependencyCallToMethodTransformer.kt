package org.jetbrains.kotlin.addDependencyCallToMethod

import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.utils.irCompanionPropertyCall
import org.jetbrains.kotlin.utils.referenceCompanionPropertyFunction
import org.jetbrains.kotlin.utils.withDeclarationIrBuilder

class AddDependencyCallToMethodTransformer(
    private val context: IrPluginContext
) : IrElementTransformerVoidWithContext() {

    companion object {
        const val CLASS_NAME = "IntValueHolder"
        const val PROPERTY_NAME = "instance"
        const val METHOD_NAME = "incrementAndGetValue"
    }

    override fun visitFunctionNew(declaration: IrFunction): IrStatement {
        if (declaration is IrConstructor) return super.visitFunctionNew(declaration)

        val (
            companionObjectSymbol,
            propertyGetter,
            funcSymbol
        ) = context.referenceCompanionPropertyFunction(CLASS_NAME, PROPERTY_NAME, METHOD_NAME)

        context.withDeclarationIrBuilder(declaration) {
            declaration.body = irBlockBody {
                +irCompanionPropertyCall(companionObjectSymbol, propertyGetter, funcSymbol)
            }
        }

        return super.visitFunctionNew(declaration)
    }
}