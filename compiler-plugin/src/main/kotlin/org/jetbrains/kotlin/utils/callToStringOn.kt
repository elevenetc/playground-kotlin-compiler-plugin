package org.jetbrains.kotlin.utils

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.functions

@OptIn(UnsafeDuringIrConstructionAPI::class)
fun callToStringOn(expression: IrExpression, context: IrPluginContext): IrCall {
    val anyClass = context.irBuiltIns.anyClass.owner
    val toStringFunctionSymbol = anyClass
        .functions
        .single { it.name.asString() == "toString" && it.valueParameters.isEmpty() }
        .symbol

    return IrCallImpl(
        startOffset = expression.startOffset,
        endOffset = expression.endOffset,
        type = context.irBuiltIns.stringType,
        symbol = toStringFunctionSymbol,
        typeArgumentsCount = 0,
        origin = null
    ).apply {
        dispatchReceiver = expression
    }
}