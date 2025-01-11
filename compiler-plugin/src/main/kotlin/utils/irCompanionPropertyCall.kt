package utils

import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGetObject
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol

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
    return irCall(method).apply {
        dispatchReceiver = irCall(propertyGetter).apply {
            dispatchReceiver = irGetObject(companion)
        }
    }.apply {
        arguments.forEachIndexed { i, e ->
            putValueArgument(i, e)
        }
    }
}

fun IrBuilderWithScope.irCompanionPropertyCall(
    triple: Triple<IrClassSymbol, IrSimpleFunction, IrSimpleFunctionSymbol>,
    arguments: List<IrExpression> = emptyList()
): IrCall {
    return irCompanionPropertyCall(triple.first, triple.second, triple.third, arguments)
}