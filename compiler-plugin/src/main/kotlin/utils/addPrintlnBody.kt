package utils

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName.topLevel
import org.jetbrains.kotlin.name.Name.identifier

@OptIn(UnsafeDuringIrConstructionAPI::class)
fun addPrintlnBody(printlnValue: String, func: IrFunction, context: IrPluginContext) {

    val printId = CallableId(topLevel(identifier("kotlin.io")), identifier("println"))
    val printlnSymbol = context.referenceFunctions(printId).first { it.owner.valueParameters.size == 1 }

    func.body = DeclarationIrBuilder(context, func.symbol).irBlockBody {
        val call = irCall(printlnSymbol, context.irBuiltIns.stringType, 1)
        call.putValueArgument(0, irString(printlnValue))
        +call
    }
}