package addPrint

import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrFunction
import utils.addPrintlnBody

class AddPrintTransformer(
    private val stringValue: String,
    private val pluginContext: IrPluginContext
) : IrElementTransformerVoidWithContext() {

    override fun visitFunctionNew(declaration: IrFunction): IrStatement {
        if (declaration is IrConstructor) return super.visitFunctionNew(declaration)
        addPrintlnBody(stringValue, declaration, pluginContext)
        return super.visitFunctionNew(declaration)
    }
}