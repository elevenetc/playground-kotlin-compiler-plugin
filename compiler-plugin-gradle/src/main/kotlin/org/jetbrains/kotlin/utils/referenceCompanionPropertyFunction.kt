package org.jetbrains.kotlin.utils

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId.Companion.topLevel
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name.identifier

/**
 * References companion property function
 * ```kotlin
 * class Foo {
 *
 *   fun bar()
 *
 *   companion object {
 *     val instance = Foo()
 *   }
 * }
 *
 * Foo.instance.bar()
 * ```
 */
fun IrPluginContext.referenceCompanionPropertyFunction(
    className: String,
    propertyName: String,
    functionName: String
): Triple<IrClassSymbol, IrSimpleFunction, IrSimpleFunctionSymbol> {
    val classId = topLevel(FqName(className))
    val companionObjectSymbol = referenceCompanionSymbol(className)
    val propertyGetter = getCompanionPropertyGetter(className, propertyName)
    val funcSymbol = referenceFunctions(CallableId(classId, identifier(functionName)))
        .firstOrNull() ?: error("Function '$functionName' not found in Foo")

    return Triple(companionObjectSymbol, propertyGetter, funcSymbol)
}