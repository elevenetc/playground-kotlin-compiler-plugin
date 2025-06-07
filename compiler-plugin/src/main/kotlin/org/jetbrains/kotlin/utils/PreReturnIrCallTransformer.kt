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

class PreReturnIrCallTransformer(
    private val pluginContext: IrPluginContext,
    private val function: IrFunction,
    private val irCall: IrCompanionPropertyFunctionCall,
    private val addReturnToArgs: Boolean = false,
) : IrTransformer<Nothing?>() {

    override fun visitReturn(
        expression: IrReturn,
        data: Nothing?
    ): IrExpression {

        if (expression.returnTargetSymbol != function.symbol) return super.visitReturn(expression, data)

        return DeclarationIrBuilder(pluginContext, function.symbol).irBlock {

            val returnType = expression.value.type
            if (addReturnToArgs && !returnType.isUnit()) {
                val tmpVal = irTemporary(expression.value, nameHint = "retVal")
                val toStringCall = callToStringOn(irGet(tmpVal), pluginContext)
                +irCompanionPropertyCall(irCall.copy(arguments = irCall.arguments + toStringCall))
                +irReturn(irGet(tmpVal))
            } else {
                +irCompanionPropertyCall(irCall)
                +expression
            }
        }
    }
}


//                val irReturnGet = irGet(irTemporary(expression))
//                val returnValue = callToStringOn(irReturnGet, pluginContext)
//                +irCompanionPropertyCall(irCall.copy(arguments = irCall.arguments + returnValue))
//                +irReturnGet

//val tmpVal = irTemporary(expression, nameHint = "retVal")
//val toStringCall = callToStringOn(irGet(tmpVal), pluginContext)
//+irCompanionPropertyCall(irCall.copy(arguments = irCall.arguments + toStringCall))
//+irReturn(irGet(tmpVal))