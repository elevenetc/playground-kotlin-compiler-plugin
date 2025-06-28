package org.jetbrains.kotlin.addCallLog

import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.IrBuilder
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irTemporary
import org.jetbrains.kotlin.ir.declarations.IrAnnotationContainer
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.name
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.util.file
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.isGetter
import org.jetbrains.kotlin.ir.util.statements
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.utils.*

class AddCallLogTransformer(
    private val excludedFqns: List<String>,
    private val excludedFiles: List<String>,
    private val context: IrPluginContext
) : IrElementTransformerVoidWithContext() {

    private val skipGetters = true

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
        traceDeclaration(declaration)
        return super.visitFunctionNew(declaration)
    }

    private fun traceDeclaration(declaration: IrFunction) {
        val fqn = declaration.safeIrFunctionFqn()
        if (skipGetters && declaration.isGetter) return
        if (excludedFiles.contains(declaration.file.name)) return
        if (excludedPatterns.any { it.matches(fqn) }) return
        if (fqn.contains(CLASS_NAME)) return
        if (declaration.hasIgnoreCallAnnotation()) return
        if (declaration.containingIrClassOrNull()?.hasIgnoreCallAnnotation() == true) return

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
                val returnTransformer = PreReturnIrCallTransformer(
                    this@AddCallLogTransformer.context,
                    declaration,
                    end.copy(arguments = endArgs)
                )
                +((irBlock as IrExpression).accept(returnTransformer, null) as IrExpression)
            }
        }
    }
}

private fun IrAnnotationContainer.hasIgnoreCallAnnotation(): Boolean {
    return hasAnnotation(ClassId(FqName("org.jetbrains.kotlin"), Name.identifier("IgnoreCallLog")))
}
