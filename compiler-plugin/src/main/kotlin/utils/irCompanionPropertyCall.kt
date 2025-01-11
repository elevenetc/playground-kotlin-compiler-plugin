package utils

import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGetObject
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
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
    companion: IrClassSymbol, propertyGetter: IrSimpleFunction, method: IrSimpleFunctionSymbol
): IrCall {
    return irCall(method).apply {
        dispatchReceiver = irCall(propertyGetter).apply {
            dispatchReceiver = irGetObject(companion)
        }
    }
}