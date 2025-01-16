package org.jetbrains.kotlin.utils

import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.isInt
import org.jetbrains.kotlin.ir.types.isString
import java.lang.Integer.parseInt

fun createExpression(type: IrType, value: String): IrExpression {
    return when {
        type.isString() -> IrConstImpl.string(
            UNDEFINED_OFFSET, UNDEFINED_OFFSET, type, value
        )

        type.isInt() -> IrConstImpl.int(
            UNDEFINED_OFFSET, UNDEFINED_OFFSET, type, parseInt(value)
        )

        else -> throw IllegalArgumentException("Unsupported type or value: type=${type.type}, value=$value")
    }
}