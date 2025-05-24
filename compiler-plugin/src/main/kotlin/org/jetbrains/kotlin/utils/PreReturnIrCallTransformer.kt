package org.jetbrains.kotlin.utils

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.visitors.IrTransformer

class PreReturnIrCallTransformer(
    private val pluginContext: IrPluginContext,
    private val function: IrFunction,
    private val irCall: IrCompanionPropertyFunctionCall
) : IrTransformer<Nothing?>() {

    override fun visitReturn(
        expression: IrReturn,
        data: Nothing?
    ): IrExpression {

        if (expression.returnTargetSymbol != function.symbol) return super.visitReturn(expression, data)

        return DeclarationIrBuilder(pluginContext, function.symbol).irBlock {
            +irCompanionPropertyCall(irCall)
            +expression
        }
    }
}
