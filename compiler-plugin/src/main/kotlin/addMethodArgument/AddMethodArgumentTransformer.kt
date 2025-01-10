package addMethodArgument

import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import utils.fqNameToIrType

class AddMethodArgumentTransformer(
    private val methodName: String,
    private val argumentName: String,
    private val argumentType: String,
    private val context: IrPluginContext
) : IrElementTransformerVoidWithContext() {

    override fun visitFunctionNew(declaration: IrFunction): IrStatement {

        if (declaration.name.asString() != methodName) return super.visitFunctionNew(declaration)

        val argType = fqNameToIrType(FqName(argumentType), context)

        declaration.addValueParameter(Name.identifier(argumentName), argType).apply {
            parent = declaration
        }

        return super.visitFunctionNew(declaration)
    }
}