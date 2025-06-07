package org.jetbrains.kotlin.utils

import org.jetbrains.kotlin.addCallLog.irGet
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.builders.IrBuilder
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrExpression

internal fun IrBuilder.getParametersMap(irFun: IrFunction, pluginContext: IrPluginContext): Map<String, IrExpression> {
    val result = mutableMapOf<String, IrExpression>()

    for (i in 0 until irFun.valueParameters.size) {
        val valueParam = irFun.valueParameters[i]
        val getValueExpression = irGet(valueParam.type, valueParam.symbol)
        val stringValue = callToStringOn(getValueExpression, pluginContext)
        result[valueParam.name.asString()] = stringValue
    }
    return result
}