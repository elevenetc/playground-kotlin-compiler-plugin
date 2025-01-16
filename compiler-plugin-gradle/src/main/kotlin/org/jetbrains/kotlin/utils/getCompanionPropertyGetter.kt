package org.jetbrains.kotlin.utils

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.companionObject
import org.jetbrains.kotlin.ir.util.properties
import org.jetbrains.kotlin.name.ClassId.Companion.topLevel
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name.identifier

@OptIn(UnsafeDuringIrConstructionAPI::class)
fun IrPluginContext.getCompanionPropertyGetter(
    className: String,
    propertyName: String
): IrSimpleFunction {
    val classId = topLevel(FqName(className))
    val companionObjectSymbol =
        referenceClass(classId)?.owner?.companionObject() ?: error("$classId class not found")
    return companionObjectSymbol.properties.firstOrNull { it.name == identifier(propertyName) }?.getter
        ?: error("$propertyName not found")
}