package addDependencyCallToMethod

import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGetObject
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.companionObject
import org.jetbrains.kotlin.ir.util.properties
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.Name.identifier

class AddDependencyCallToMethodTransformer(
    private val pluginContext: IrPluginContext
) : IrElementTransformerVoidWithContext() {

    companion object {
        const val CLASS_NAME = "IntValueHolder"
        const val STATIC_METHOD = "incrementAndGetValue"
        const val STATIC_PROPERTY = "instance"
    }

    @OptIn(UnsafeDuringIrConstructionAPI::class)
    override fun visitFunctionNew(declaration: IrFunction): IrStatement {
        if (declaration is IrConstructor) return super.visitFunctionNew(declaration)

        val builder = DeclarationIrBuilder(pluginContext, declaration.symbol)
        val dependencyClassId = ClassId.topLevel(FqName(CLASS_NAME))
        val dependencyClassSymbol = pluginContext.referenceClass(dependencyClassId) ?: error("could not find class")

        val companionObjectSymbol = pluginContext.referenceClass(dependencyClassId)?.owner?.companionObject()
        val instanceGetter =
            companionObjectSymbol?.properties?.firstOrNull { it.name == Name.identifier(STATIC_PROPERTY) }?.getter
                ?: error("$STATIC_PROPERTY not found")

        val doCallCallableId = CallableId(dependencyClassId, identifier(STATIC_METHOD))
        val doCallFunctionSymbol = pluginContext.referenceFunctions(doCallCallableId)
            .firstOrNull() ?: error("Function '$STATIC_METHOD' not found in Foo")

        val instAccess = builder.irCall(instanceGetter).apply {
            dispatchReceiver = builder.irGetObject(dependencyClassSymbol.owner.companionObject()!!.symbol)
        }

        val doCallInvocation = builder.irCall(doCallFunctionSymbol).apply {
            dispatchReceiver = instAccess
        }

        declaration.body = builder.irBlockBody {
            +doCallInvocation
        }

        return super.visitFunctionNew(declaration)
    }
}