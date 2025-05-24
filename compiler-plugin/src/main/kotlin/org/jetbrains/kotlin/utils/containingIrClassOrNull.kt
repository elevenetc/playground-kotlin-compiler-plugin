package org.jetbrains.kotlin.utils

import org.jetbrains.kotlin.ir.declarations.IrFunction

internal fun IrFunction.containingIrClassOrNull() = parent as? org.jetbrains.kotlin.ir.declarations.IrClass