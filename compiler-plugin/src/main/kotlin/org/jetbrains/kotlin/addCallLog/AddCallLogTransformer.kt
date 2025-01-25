package org.jetbrains.kotlin.addCallLog

import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.buildVariable
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.expressions.impl.IrTryImpl
import org.jetbrains.kotlin.ir.util.fqNameForIrSerialization
import org.jetbrains.kotlin.ir.util.statements
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.utils.irCompanionPropertyCall
import org.jetbrains.kotlin.utils.referenceCompanionPropertyFunction
import org.jetbrains.kotlin.utils.withDeclarationIrBuilder

class AddCallLogTransformer(
    excludedFqns: List<String>,
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
        if (excludedPatterns.any { it.matches(fqn) }) return
        if (fqn.contains(CLASS_NAME)) return

        val typeUnit = context.irBuiltIns.unitType
        val typeThrowable = context.irBuiltIns.throwableType
        val start = context.referenceCompanionPropertyFunction(CLASS_NAME, STATIC_PROPERTY, START_METHOD)
        val end = context.referenceCompanionPropertyFunction(CLASS_NAME, STATIC_PROPERTY, END_METHOD)

        context.withDeclarationIrBuilder(declaration) {
            val startArgs = listOf(irString(fqn))
            val endArgs = mutableListOf<IrExpression>()
            val sourceStatements = declaration.body?.statements ?: emptyList()
            declaration.body = irBlockBody {

                val startUuid = irTemporary(irCompanionPropertyCall(start, startArgs))
                endArgs.add(irGet(startUuid))
                val endIr = irCompanionPropertyCall(end, endArgs)


                val tryBlock = irBlock(resultType = declaration.returnType) {
                    for (statement in sourceStatements) +statement
                    if (declaration.returnType == typeUnit) +endIr
                }.transform(ReturnTransformer(this@AddCallLogTransformer.context, declaration, endIr), null)

                buildVariable(
                    scope.getLocalDeclarationParent(), startOffset, endOffset, IrDeclarationOrigin.CATCH_PARAMETER,
                    Name.identifier("t"), typeThrowable
                )

                +IrTryImpl(startOffset, endOffset, tryBlock.type).also { irTry ->
                    irTry.tryResult = tryBlock
                    /**
                     * TODO: handle catch block properly
                     * Currently the code fails with no method exception at irCatch call
                     */
//                    irTry.catches += irCatch(throwable, irBlock {
//                        //+endIr
//                        +irThrow(irGet(throwable))
//                    })
                }
            }
        }
    }
}

private class ReturnTransformer(
    private val pluginContext: IrPluginContext,
    private val function: IrFunction,
    private val irEnd: IrCall
) : IrElementTransformerVoidWithContext() {
    override fun visitReturn(expression: IrReturn): IrExpression {
        if (expression.returnTargetSymbol != function.symbol) return super.visitReturn(expression)

        return DeclarationIrBuilder(pluginContext, function.symbol).irBlock {
            val result = irTemporary(expression.value)
            +irEnd
            +expression.apply {
                value = irGet(result)
            }
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
