package org.jetbrains.kotlin.utils

import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrVarargImpl
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.constructors

fun IrBuilderWithScope.irStringStringMap(values: Map<String, String>): IrExpression? {

    if (values.isEmpty()) return null

    val mapOfFun = context.irBuiltIns.mutableMapClass.owner
    val mapEntry = context.irBuiltIns.mutableMapEntryClass.owner

    val irValues = values.map { (key, value) ->

        val pairConstructor = mapEntry.constructors.first()
        irCall(pairConstructor).apply {
            putTypeArgument(0, context.irBuiltIns.stringType)
            putTypeArgument(1, context.irBuiltIns.stringType)
            putValueArgument(0, irString(key))
            putValueArgument(1, irString(value))
        }
    }

    return irCall(mapOfFun.constructors.first()).apply {
        putTypeArgument(0, context.irBuiltIns.stringType)
        putTypeArgument(1, context.irBuiltIns.stringType)
        putValueArgument(
            0, IrVarargImpl(
                startOffset,
                endOffset,
                context.irBuiltIns.arrayClass.typeWith(
                    mapEntry.typeWith(
                        context.irBuiltIns.stringType, context.irBuiltIns.stringType
                    )
                ),
                mapEntry.typeWith(context.irBuiltIns.stringType, context.irBuiltIns.stringType),
                irValues
            )
        )
    }
}