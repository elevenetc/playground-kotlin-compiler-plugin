package org.jetbrains.kotlin.addCallLog

import org.jetbrains.kotlin.ir.builders.IrBuilder
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.symbols.IrValueSymbol
import org.jetbrains.kotlin.ir.types.IrType

fun IrBuilder.irGet(variable: IrVariable): IrGetValue {
    return irGet(variable.type, variable.symbol)
}

fun IrBuilder.irGet(type: IrType, variable: IrValueSymbol): IrGetValue {
    return IrGetValueImpl(startOffset, endOffset, type, variable)
}