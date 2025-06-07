package org.jetbrains.kotlin.utils

import org.jetbrains.kotlin.ir.builders.IrBuilder
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irGetObject
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI

/**
 * Takes companion property function:
 * ```kotlin
 * class Foo {
 *
 *   fun bar()
 *
 *   companion object {
 *     val instance = Foo()
 *   }
 * }
 * ```
 * And returns new ir call
 * ```kotlin
 * Foo.instance.bar()
 * ```
 */
fun IrBuilderWithScope.irCompanionPropertyCall(
    companion: IrClassSymbol,
    propertyGetter: IrSimpleFunction,
    method: IrSimpleFunctionSymbol,
    arguments: List<IrExpression> = emptyList()
): IrCall {
    val builder = this as IrBuilder
    return builder.irCall(method).apply {
        dispatchReceiver = (builder).irCall(propertyGetter).apply {
            dispatchReceiver = irGetObject(companion)
        }
    }.apply {
        arguments.forEachIndexed { i, e ->
            putValueArgument(i, e)
        }
    }
}

fun IrBuilderWithScope.irCompanionPropertyCall(
    irCall: IrCompanionPropertyFunctionCall
): IrCall {
    return irCompanionPropertyCall(irCall.irClass, irCall.irProperty, irCall.irFun, irCall.arguments)
}

fun IrBuilder.irCall(function: IrSimpleFunction): IrCall {
    val callee = function.symbol
    return irCall(callee)
}

@OptIn(UnsafeDuringIrConstructionAPI::class)
private fun IrBuilder.irCall(callee: IrSimpleFunctionSymbol): IrCall {
    val type = callee.owner.returnType
    val typeArgumentsCount = callee.owner.typeParameters.size
    val origin = null
    return IrCallImpl(
        startOffset = startOffset,
        endOffset = endOffset,
        type = type,
        symbol = callee,
        typeArgumentsCount = typeArgumentsCount,
        origin = origin
    )
}