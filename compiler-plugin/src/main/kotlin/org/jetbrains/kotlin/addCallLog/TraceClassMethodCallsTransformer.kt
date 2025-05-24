package org.jetbrains.kotlin.addCallLog

import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.IrBuilder
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irTemporary
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.util.fqNameForIrSerialization
import org.jetbrains.kotlin.ir.util.statements
import org.jetbrains.kotlin.utils.*

class TraceClassMethodCallsTransformer(
    private val traceClassFqns: List<String>,
    private val context: IrPluginContext
) : IrElementTransformerVoidWithContext() {

    companion object {
        const val CLASS_NAME = "org.jetbrains.kotlin.ClassMethodsCallsTracer"
        const val STATIC_PROPERTY = "instance"
        const val START_METHOD = "start"
        const val END_METHOD = "end"
    }

    override fun visitFunctionNew(declaration: IrFunction): IrStatement {
        if (traceClassFqns.isEmpty() || declaration is IrConstructor) return super.visitFunctionNew(declaration)
        wrapDeclarationWithLogs(declaration)
        return super.visitFunctionNew(declaration)
    }

    private fun wrapDeclarationWithLogs(declaration: IrFunction) {

        val functionFqn = declaration.safeIrFunctionFqn()
        val containingClassFqn = declaration.containingIrClassOrNull()?.fqNameForIrSerialization?.toString()

        if (containingClassFqn == null) return
        if (!traceClassFqns.contains(containingClassFqn)) return
        if (functionFqn.contains(CLASS_NAME)) return

        val start = context.referenceCompanionPropertyFunction(CLASS_NAME, STATIC_PROPERTY, START_METHOD)
        val end = context.referenceCompanionPropertyFunction(CLASS_NAME, STATIC_PROPERTY, END_METHOD)

        context.withDeclarationIrBuilder(declaration) {

            val builder = this as IrBuilder


            val startArgs = listOfNotNull(
                builder.irString(containingClassFqn),
                builder.irString(functionFqn),
                irStringStringMap(declaration.getParametersMap())
            )

            val endArgs = mutableListOf<IrExpression>()
            val sourceStatements = declaration.body?.statements ?: emptyList()

            declaration.body = irBlockBody {

                val startId = irTemporary(irCompanionPropertyCall(start.copy(arguments = startArgs)))

                endArgs.add(builder.irString(containingClassFqn))
                endArgs.add(builder.irGet(startId))

                val irBlock = irBlock(resultType = declaration.returnType) {
                    val hasReturn = sourceStatements.any { it is IrReturn }
                    for (statement in sourceStatements) +statement

                    if (!hasReturn) {
                        +irCompanionPropertyCall(end.copy(arguments = endArgs))
                    }
                }
                val returnTransformer = PreReturnIrCallTransformer(
                    this@TraceClassMethodCallsTransformer.context,
                    declaration,
                    end.copy(arguments = endArgs)
                )
                +((irBlock as IrExpression).accept(returnTransformer, null) as IrExpression)
            }
        }
    }
}