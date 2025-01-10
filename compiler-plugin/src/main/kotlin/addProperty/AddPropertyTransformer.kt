package addProperty

import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.symbols.impl.IrFieldSymbolImpl
import org.jetbrains.kotlin.ir.symbols.impl.IrPropertySymbolImpl
import org.jetbrains.kotlin.ir.util.addChild
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import utils.createExpression
import utils.fqNameToIrType

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
        val fieldSymbol = IrFieldSymbolImpl()
        val irType = fqNameToIrType(FqName(propertyType), context)

        val irField = context.irFactory.createField(
            startOffset = irClass.startOffset,
            endOffset = irClass.endOffset,
            origin = IrDeclarationOrigin.PROPERTY_BACKING_FIELD,
            name = propertyName,
            visibility = DescriptorVisibilities.PUBLIC,
            symbol = fieldSymbol,
            type = irType,
            isFinal = true,
            isStatic = false,
            isExternal = false
        ).apply {
            initializer = irClass.factory.createExpressionBody(
                startOffset,
                endOffset,
                createExpression(irType, propertyValue)
            )
            parent = irClass
        }

        val irProperty = context.irFactory.createProperty(
            startOffset = irClass.startOffset,
            endOffset = irClass.endOffset,
            origin = IrDeclarationOrigin.DEFAULT_PROPERTY_ACCESSOR,
            symbol = IrPropertySymbolImpl(),
            name = propertyName,
            visibility = DescriptorVisibilities.PUBLIC,
            modality = Modality.FINAL,
            isVar = false,
            isConst = false,
            isLateinit = false,
            isDelegated = false,
        ).apply {
            backingField = irField
        }

        irClass.addChild(irProperty)
    }
}