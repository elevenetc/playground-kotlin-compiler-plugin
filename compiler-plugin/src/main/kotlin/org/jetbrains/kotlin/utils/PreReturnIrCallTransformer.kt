package org.jetbrains.kotlin.utils

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irTemporary
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.types.isUnit
import org.jetbrains.kotlin.ir.visitors.IrTransformer

/**
 * When [addReturnValueToArgs] is true, then return value is passed to [preReturnCall] as an argument
 */
class PreReturnIrCallTransformer(
    private val pluginContext: IrPluginContext,
    private val function: IrFunction,
    private val preReturnCall: IrCompanionPropertyFunctionCall,
    private val addReturnValueToArgs: Boolean = false,
) : IrTransformer<Nothing?>() {

    override fun visitReturn(
        expression: IrReturn,
        data: Nothing?
    ): IrExpression {

        if (expression.returnTargetSymbol != function.symbol) return super.visitReturn(expression, data)

        return DeclarationIrBuilder(pluginContext, function.symbol).irBlock {

            val returnType = expression.value.type
            if (addReturnValueToArgs && !returnType.isUnit()) {
                val tmpVal = irTemporary(expression.value, nameHint = "retVal")
                val toStringCall = callToStringOn(irGet(tmpVal), pluginContext)
                +irCompanionPropertyCall(preReturnCall.copy(arguments = preReturnCall.arguments + toStringCall))
                +irReturn(irGet(tmpVal))
            } else {
                val tmpVal = irTemporary(expression.value, nameHint = "retVal")
                +irCompanionPropertyCall(preReturnCall)
                +irReturn(irGet(tmpVal))
            }
        }
    }
}