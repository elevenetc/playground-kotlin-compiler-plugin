package utils

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

@OptIn(UnsafeDuringIrConstructionAPI::class)
fun addPrintlnBody(printlnValue: String, func: IrFunction, pluginContext: IrPluginContext) {

    val printId = CallableId(FqName.topLevel(Name.identifier("kotlin.io")), Name.identifier("println"))
    val printlnSymbol = pluginContext
        .referenceFunctions(printId)
        .first { it.owner.valueParameters.size == 1 }

    val builder = DeclarationIrBuilder(pluginContext, func.symbol)
    val body = builder.irBlockBody {}
    val element = builder.irCall(printlnSymbol, pluginContext.irBuiltIns.stringType, 1)
    element.putValueArgument(0, builder.irString(printlnValue))
    body.statements.add(element)
    func.body = body
}