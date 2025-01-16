package org.jetbrains.kotlin.utils

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classifierOrNull
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.fqNameForIrSerialization
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName

@OptIn(UnsafeDuringIrConstructionAPI::class)
fun irTypeToString(type: IrType): String {
    val classifier = type.classifierOrNull
    return when {
        classifier is IrClassSymbol -> classifier.owner.fqNameForIrSerialization.asString()
        else -> throw IllegalArgumentException("Unsupported type: $type")
    }
}

fun stringToIrType(type: String, context: IrPluginContext): IrType {
    val builtIns = context.irBuiltIns
    return when (type) {
        "kotlin.Int" -> builtIns.intType
        "kotlin.Boolean" -> builtIns.booleanType
        "kotlin.Float" -> builtIns.floatType
        "kotlin.Double" -> builtIns.doubleType
        "kotlin.String" -> builtIns.stringType
        else -> {
            val symbol = context.referenceClass(ClassId.topLevel(FqName(type)))
                ?: throw IllegalArgumentException("Cannot resolve type: $type")
            symbol.defaultType
        }
    }
}

fun fqNameToIrType(fqName: FqName, context: IrPluginContext): IrType {
    val classSymbol: IrClassSymbol = context.referenceClass(ClassId.topLevel(fqName))
        ?: throw IllegalArgumentException("Cannot resolve class for FqName: $fqName")
    return classSymbol.defaultType
}