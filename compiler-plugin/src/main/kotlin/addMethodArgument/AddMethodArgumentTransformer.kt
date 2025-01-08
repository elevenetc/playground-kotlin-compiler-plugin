package addMethodArgument

import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.symbols.impl.IrValueParameterSymbolImpl
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import utils.fqNameToIrType

class AddMethodArgumentTransformer(
    private val methodName: String,
    private val argumentName: String,
    private val argumentType: String,
    private val pluginContext: IrPluginContext
) : IrElementTransformerVoidWithContext() {

    override fun visitFunctionNew(declaration: IrFunction): IrStatement {

        if (declaration.name.asString() != methodName) return super.visitFunctionNew(declaration)

        val argType = fqNameToIrType(FqName(argumentType), pluginContext)

        val param = pluginContext.irFactory.createValueParameter(
            startOffset = declaration.startOffset,
            endOffset = declaration.endOffset,
            origin = declaration.origin,
            symbol = IrValueParameterSymbolImpl(),
            name = Name.identifier(argumentName),
            type = argType,
            isAssignable = false,
            index = 0,
            varargElementType = null,
            isCrossinline = false,
            isNoinline = false,
            isHidden = false
        ).also {
            it.parent = declaration
        }

        declaration.valueParameters += param

        return super.visitFunctionNew(declaration)
    }
}