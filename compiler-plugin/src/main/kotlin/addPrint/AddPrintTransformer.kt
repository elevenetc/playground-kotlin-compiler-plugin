package addPrint

import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

class AddPrintTransformer(
    private val stringValue: String,
    private val pluginContext: IrPluginContext
) : IrElementTransformerVoidWithContext() {


    @OptIn(UnsafeDuringIrConstructionAPI::class)
    override fun visitFunctionNew(declaration: IrFunction): IrStatement {
        if (declaration is IrConstructor) return super.visitFunctionNew(declaration)

        val printId = CallableId(FqName.topLevel(Name.identifier("kotlin.io")), Name.identifier("println"))
        val printlnSymbol = pluginContext
            .referenceFunctions(printId)
            .first { it.owner.valueParameters.size == 1 }

        val builder = DeclarationIrBuilder(pluginContext, declaration.symbol)


        val newBody = builder.irBlockBody {}
        val element = builder.irCall(printlnSymbol, pluginContext.irBuiltIns.stringType, 1)
        element.putValueArgument(0, builder.irString(stringValue))
        newBody.statements.add(element)
        declaration.body = newBody


        return super.visitFunctionNew(declaration)
    }
}