package org.jetbrains.kotlin.utils

import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.util.fqNameForIrSerialization

/**
 * [IrFunction.callableId] might throw exception in set of cases
 * So [fqNameForIrSerialization] is used instead
 */
internal fun IrFunction.safeIrFunctionFqn(): String {
    return this.fqNameForIrSerialization.toString()
}