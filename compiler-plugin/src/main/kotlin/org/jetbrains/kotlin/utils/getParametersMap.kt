package org.jetbrains.kotlin.utils

import org.jetbrains.kotlin.ir.declarations.IrFunction

internal fun IrFunction.getParametersMap(): Map<String, String> {
    val result = mutableMapOf<String, String>()
    valueParameters.forEach { param ->
        result[param.name.toString()] = "${param.defaultValue}:${param.type}"
    }
    return result
}