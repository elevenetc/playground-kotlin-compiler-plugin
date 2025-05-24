package org.jetbrains.kotlin.utils

import org.jetbrains.kotlin.ir.builders.IrBuilder
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl

internal fun IrBuilder.irString(value: String): IrConst {
    return IrConstImpl.string(startOffset, endOffset, context.irBuiltIns.stringType, value)
}