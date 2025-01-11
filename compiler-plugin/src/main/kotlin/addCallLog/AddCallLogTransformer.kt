package addCallLog

import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.util.callableId
import org.jetbrains.kotlin.ir.util.statements
import utils.irCompanionPropertyCall
import utils.referenceCompanionPropertyFunction
import utils.withDeclarationIrBuilder

class AddCallLogTransformer(
    private val context: IrPluginContext
) : IrElementTransformerVoidWithContext() {

    companion object {
        const val CLASS_NAME = "CallLogger"
        const val STATIC_PROPERTY = "instance"
        const val START_METHOD = "start"
        const val END_METHOD = "end"
    }

    override fun visitFunctionNew(declaration: IrFunction): IrStatement {
        if (declaration is IrConstructor) return super.visitFunctionNew(declaration)
        wrapDeclarationWithLogs(declaration)
        return super.visitFunctionNew(declaration)
    }

    private fun wrapDeclarationWithLogs(declaration: IrFunction) {
        val start = context.referenceCompanionPropertyFunction(CLASS_NAME, STATIC_PROPERTY, START_METHOD)
        val end = context.referenceCompanionPropertyFunction(CLASS_NAME, STATIC_PROPERTY, END_METHOD)
        val funId = declaration.callableId.toString()

        context.withDeclarationIrBuilder(declaration) {
            val startArgs = listOf(irString(funId))
            val endArgs = mutableListOf<IrExpression>()
            val sourceStatements = declaration.body?.statements ?: emptyList()
            declaration.body = irBlockBody {
                val startUuid = irTemporary(irCompanionPropertyCall(start, startArgs))
                endArgs.add(irGet(startUuid))
                +irBlock {
                    for (statement in sourceStatements) +statement
                }
                +irCompanionPropertyCall(end, endArgs)
            }
        }
    }
}