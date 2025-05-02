package org.jetbrains.kotlin.addCallLog

//import org.jetbrains.kotlin.ir.visitors.IrElementVisitor
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.IrBuilder
import org.jetbrains.kotlin.ir.builders.declarations.buildVariable
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irTemporary
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrTryImpl
import org.jetbrains.kotlin.ir.util.fqNameForIrSerialization
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.statements
import org.jetbrains.kotlin.ir.visitors.IrTransformer
import org.jetbrains.kotlin.ir.visitors.IrVisitor
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.utils.irCompanionPropertyCall
import org.jetbrains.kotlin.utils.referenceCompanionPropertyFunction
import org.jetbrains.kotlin.utils.withDeclarationIrBuilder

class AddCallLogTransformer(
    private val excludedFqns: List<String>,
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
        if (declaration.hasAnnotation(ClassId(FqName("org.jetbrains.kotlin"), Name.identifier("IgnoreCallLog")))) return

        val typeUnit = context.irBuiltIns.unitType
        val typeThrowable = context.irBuiltIns.throwableType
        val start = context.referenceCompanionPropertyFunction(CLASS_NAME, STATIC_PROPERTY, START_METHOD)
        val end = context.referenceCompanionPropertyFunction(CLASS_NAME, STATIC_PROPERTY, END_METHOD)



        context.withDeclarationIrBuilder(declaration) {

            val builder = this as IrBuilder

            val startArgs = listOf((this as IrBuilder).irString(fqn))
            val endArgs = mutableListOf<IrExpression>()
            val sourceStatements = declaration.body?.statements ?: emptyList()
            declaration.body = irBlockBody {

                val startUuid = irTemporary(irCompanionPropertyCall(start, startArgs))
                endArgs.add(builder.irGet(startUuid))
                val endIr = irCompanionPropertyCall(end, endArgs)

                val irBlock = irBlock(resultType = declaration.returnType) {
                    for (statement in sourceStatements) +statement
                    if (declaration.returnType == typeUnit) +endIr
                }
                val returnTransformer = makeTransformer(
                    this@AddCallLogTransformer.context,
                    declaration,
                    endIr
                ) as IrVisitor<IrElement, Nothing?>
                println("1: $returnTransformer")
                val exppp = irBlock as IrExpression
                val tryBlockTransformed = exppp.accept(returnTransformer, null) as IrExpression
//                val tryBlockTransformed = exppp.transform(
//                    transformer = returnTransformer,
//                    data = null
//                )
                println("2: $returnTransformer")

                buildVariable(
                    scope.getLocalDeclarationParent(), startOffset, endOffset, IrDeclarationOrigin.CATCH_PARAMETER,
                    Name.identifier("t"), typeThrowable
                )

                println("3: $returnTransformer")

                +IrTryImpl(startOffset, endOffset, tryBlockTransformed.type).also { irTry ->
                    irTry.tryResult = tryBlockTransformed
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

fun makeTransformer(context: IrPluginContext, irFunc: IrFunction, irCall: IrCall): IrTransformer<Nothing?> {
    return ReturnTransformer(context, irFunc, irCall) as IrTransformer<Nothing?>
}

fun IrBuilder.irString(value: String): IrConstImpl {
    return IrConstImpl.string(startOffset, endOffset, context.irBuiltIns.stringType, value)
}

private class ReturnTransformer(
    private val pluginContext: IrPluginContext,
    private val function: IrFunction,
    private val irEnd: IrCall
) : IrTransformer<Nothing?>() {
    //) : IrTypeTransformerVoid() {
//) : IrElementTransformerVoidWithContext() {

    override fun visitReturn(
        expression: IrReturn,
        data: Nothing?
    ): IrExpression {
        if (expression.returnTargetSymbol != function.symbol) return super.visitReturn(expression, data)

        return DeclarationIrBuilder(pluginContext, function.symbol).irBlock {
            val result = irTemporary(expression.value)
            +irEnd
            +expression.apply {
                value = irGet(result)
            }
        }
    }

    //    override fun visitReturn(expression: IrReturn): IrExpression {
//        if (expression.returnTargetSymbol != function.symbol) return super.visitReturn(expression)
//
//        return DeclarationIrBuilder(pluginContext, function.symbol).irBlock {
//            val result = irTemporary(expression.value)
//            +irEnd
//            +expression.apply {
//                value = irGet(result)
//            }
//        }
//    }
//
//    override fun visitElement(
//        element: IrElement,
//        data: IrExpression
//    ): IrExpression {
//        return element.accept(this, data)
//    }


//    override fun <Type : IrType?> transformTypeRecursively(
//        container: IrElement,
//        type: Type
//    ): Type {
//        TODO("Not yet implemented")
//    }

    //    override fun visitReturn(expression: IrReturn) {
//        super.visitReturn(expression)
//    }
//    override fun visitElement(
//        element: IrElement,
//        data: Nothing?
//    ): IrElement {
//        TODO("Not yet implemented")
//    }

}

/**
 * [IrFunction.callableId] might throw exception in set of cases
 * So [fqNameForIrSerialization] is used instead
 */
private fun IrFunction.safeFqn(): String {
    return this.fqNameForIrSerialization.toString()
}
