package addMethod

import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.util.addChild
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import utils.addPrintlnBody
import utils.fqNameToIrType

class AddMethodTransformer(
    private val methodName: String,
    private val isStatic: Boolean,
    private val context: IrPluginContext
) : IrElementTransformerVoidWithContext() {

    override fun visitClassNew(declaration: IrClass): IrStatement {
        addMethod(declaration)
        return super.visitClassNew(declaration)
    }

    private fun addMethod(irClass: IrClass) {
        val methodName = Name.identifier(methodName)
        val returnType = fqNameToIrType(FqName("kotlin.Unit"), context)
        val factory = context.irFactory

        val irFunction = factory.buildFun {
            this.name = methodName
            this.returnType = returnType
        }.apply {
            if (!isStatic) dispatchReceiverParameter = irClass.thisReceiver
            addPrintlnBody("$methodName", this, context)
        }

        irClass.addChild(irFunction)
    }
}