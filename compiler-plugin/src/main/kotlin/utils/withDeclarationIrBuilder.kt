package utils

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.symbols.IrSymbol

inline fun <R> IrPluginContext.withDeclarationIrBuilder(
    irSymbol: IrSymbol,
    block: DeclarationIrBuilder.() -> R
): R {
    val builder = DeclarationIrBuilder(this, irSymbol)
    return block(builder)
}

inline fun <R> IrPluginContext.withDeclarationIrBuilder(
    irFunction: IrFunction,
    block: DeclarationIrBuilder.() -> R
): R {
    return withDeclarationIrBuilder(irFunction.symbol, block)
}