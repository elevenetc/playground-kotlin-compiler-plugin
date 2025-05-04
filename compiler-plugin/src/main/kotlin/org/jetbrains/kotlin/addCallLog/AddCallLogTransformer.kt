package org.jetbrains.kotlin.addCallLog

import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.IrBuilder
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irTemporary
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.name
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.util.file
import org.jetbrains.kotlin.ir.util.fqNameForIrSerialization
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.statements
import org.jetbrains.kotlin.ir.visitors.IrTransformer
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.utils.IrCompanionPropertyFunctionCall
import org.jetbrains.kotlin.utils.irCompanionPropertyCall
import org.jetbrains.kotlin.utils.referenceCompanionPropertyFunction
import org.jetbrains.kotlin.utils.withDeclarationIrBuilder

class AddCallLogTransformer(
    private val excludedFqns: List<String>,
    private val excludedFiles: List<String>,
    private val context: IrPluginContext
) : IrElementTransformerVoidWithContext() {
    private val excludedPatterns: List<Regex> = excludedFqns.map { pattern ->
        pattern.replace(".", "\\.").replace("*", ".*").toRegex()
    }

    companion object {
        const val CLASS_NAME = "org.jetbrains.kotlin.CallLogger"
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
        val fqn = declaration.safeFqn()
        if (excludedFiles.contains(declaration.file.name)) return
        if (excludedPatterns.any { it.matches(fqn) }) return
        if (fqn.contains(CLASS_NAME)) return
        if (declaration.hasAnnotation(ClassId(FqName("org.jetbrains.kotlin"), Name.identifier("IgnoreCallLog")))) return

        val start = context.referenceCompanionPropertyFunction(CLASS_NAME, STATIC_PROPERTY, START_METHOD)
        val end = context.referenceCompanionPropertyFunction(CLASS_NAME, STATIC_PROPERTY, END_METHOD)

        context.withDeclarationIrBuilder(declaration) {

            val builder = this as IrBuilder

            val startArgs = listOf((this as IrBuilder).irString(fqn))
            val endArgs = mutableListOf<IrExpression>()
            val sourceStatements = declaration.body?.statements ?: emptyList()

            declaration.body = irBlockBody {

                val startUuidResult = irTemporary(irCompanionPropertyCall(start.copy(arguments = startArgs)))
                endArgs.add(builder.irGet(startUuidResult))

                val irBlock = irBlock(resultType = declaration.returnType) {
                    val hasReturn = sourceStatements.any { it is IrReturn }
                    for (statement in sourceStatements) +statement

                    if (!hasReturn) {
                        +irCompanionPropertyCall(end.copy(arguments = endArgs))
                    }
                }
                val returnTransformer = AddPreReturnIrCallTransformer(
                    this@AddCallLogTransformer.context,
                    declaration,
                    end.copy(arguments = endArgs)
                )
                +((irBlock as IrExpression).accept(returnTransformer, null) as IrExpression)
            }
        }
    }
}

fun IrBuilder.irString(value: String): IrConstImpl {
    return IrConstImpl.string(startOffset, endOffset, context.irBuiltIns.stringType, value)
}

private class AddPreReturnIrCallTransformer(
    private val pluginContext: IrPluginContext,
    private val function: IrFunction,
    private val irCall: IrCompanionPropertyFunctionCall
) : IrTransformer<Nothing?>() {

    override fun visitReturn(
        expression: IrReturn,
        data: Nothing?
    ): IrExpression {

        if (expression.returnTargetSymbol != function.symbol) return super.visitReturn(expression, data)

        return DeclarationIrBuilder(pluginContext, function.symbol).irBlock {
            +irCompanionPropertyCall(irCall)
            +expression
        }
    }
}

/**
 * [IrFunction.callableId] might throw exception in set of cases
 * So [fqNameForIrSerialization] is used instead
 */
private fun IrFunction.safeFqn(): String {
    return this.fqNameForIrSerialization.toString()
}
