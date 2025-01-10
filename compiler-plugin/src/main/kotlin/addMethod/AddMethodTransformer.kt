package addMethod

import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrFactory
import org.jetbrains.kotlin.ir.symbols.impl.IrSimpleFunctionSymbolImpl
import org.jetbrains.kotlin.ir.util.addChild
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import utils.addPrintlnBody
import utils.fqNameToIrType

class AddMethodTransformer(
    private val methodName: String,
    private val isStatic: Boolean,
    private val pluginContext: IrPluginContext
) : IrElementTransformerVoidWithContext() {

    override fun visitClassNew(declaration: IrClass): IrStatement {
        addMethod(declaration)
        return super.visitClassNew(declaration)
    }

    private fun addMethod(irClass: IrClass) {
        val methodName = Name.identifier(methodName)
        val symbol = IrSimpleFunctionSymbolImpl()
        val returnType = fqNameToIrType(FqName("kotlin.Unit"), pluginContext)

        val irFunction = pluginContext.irFactory.createSimpleFunction(
            startOffset = irClass.startOffset,
            endOffset = irClass.endOffset,
            origin = IrDeclarationOrigin.DEFINED,
            name = methodName,
            visibility = DescriptorVisibilities.PUBLIC,
            isInline = false,
            isExpect = false,
            returnType = returnType,
            modality = Modality.FINAL,
            symbol = symbol,
            isTailrec = false,
            isSuspend = false,
            isOperator = false,
            isInfix = false,
        ).apply {
            if (!isStatic) dispatchReceiverParameter = irClass.thisReceiver
            addPrintlnBody("$methodName", this, pluginContext)
        }
        irClass.addChild(irFunction)
    }
}