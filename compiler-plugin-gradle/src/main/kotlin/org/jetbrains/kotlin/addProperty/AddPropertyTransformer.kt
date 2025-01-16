package org.jetbrains.kotlin.addProperty

import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.declarations.buildField
import org.jetbrains.kotlin.ir.builders.declarations.buildProperty
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.util.addChild
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.utils.createExpression
import org.jetbrains.kotlin.utils.fqNameToIrType

class AddPropertyTransformer(
    private val propertyName: String,
    private val propertyType: String,
    private val propertyValue: String,
    private val context: IrPluginContext
) : IrElementTransformerVoidWithContext() {

    override fun visitClassNew(declaration: IrClass): IrStatement {
        addProperty(declaration)
        return super.visitClassNew(declaration)
    }

    private fun addProperty(irClass: IrClass) {
        val propertyName = Name.identifier(propertyName)
        val irType = fqNameToIrType(FqName(propertyType), context)
        val factory = context.irFactory

        val field = factory.buildField {
            name = propertyName
            type = irType
        }.apply {
            initializer = irClass.factory.createExpressionBody(
                startOffset,
                endOffset,
                createExpression(irType, propertyValue)
            )
            parent = irClass
        }

        val property = factory.buildProperty {
            name = propertyName
        }.apply { backingField = field }

        irClass.addChild(property)
    }
}