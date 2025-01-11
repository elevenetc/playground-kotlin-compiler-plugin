package utils

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.companionObject
import org.jetbrains.kotlin.name.ClassId.Companion.topLevel
import org.jetbrains.kotlin.name.FqName

@OptIn(UnsafeDuringIrConstructionAPI::class)
fun IrPluginContext.referenceCompanionClass(className: String): IrClass {
    val classId = topLevel(FqName(className))
    return referenceClass(classId)?.owner?.companionObject() ?: error("Class $classId not found")
}

fun IrPluginContext.referenceCompanionSymbol(className: String): IrClassSymbol {
    return referenceCompanionClass(className).symbol
}