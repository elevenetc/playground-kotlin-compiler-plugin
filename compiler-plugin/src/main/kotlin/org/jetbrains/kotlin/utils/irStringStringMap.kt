package org.jetbrains.kotlin.utils

import org.jetbrains.kotlin.backend.common.extensions.FirIncompatiblePluginAPI
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.IrGeneratorContext
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrVarargImpl
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.typeOrNull
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.isVararg
import org.jetbrains.kotlin.ir.util.substitute
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

@OptIn(FirIncompatiblePluginAPI::class)
fun IrBuilderWithScope.irStringStringMap(
    values: Map<String, IrExpression>,
    pluginContext: IrPluginContext
): IrExpression? {
    val irBuiltIns = context.irBuiltIns
    val stringType = irBuiltIns.stringType

    if (values.isEmpty()) {
        return null
    }

    // 1. Get Pair<String, String> type and its constructor
    val pairClassSymbol = pluginContext.referenceClass(ClassId.topLevel(FqName("kotlin.Pair")))
        ?: error("kotlin.Pair class not found")
    val pairConstructorSymbol = pairClassSymbol.constructors.firstOrNull()
        ?: error("kotlin.Pair constructor not found")
    val pairStringStringType = pairClassSymbol.typeWith(stringType, stringType)

    // 2. Create IrCall expressions for each Pair(key, value)
    val irPairExpressions = values.map { (key, value) ->
        irCall(pairConstructorSymbol).apply {
            putTypeArgument(0, stringType) // For Pair's first type parameter
            putTypeArgument(1, stringType) // For Pair's second type parameter
            putValueArgument(0, irString(key))
            putValueArgument(1, value)
            //putValueArgument(1, irString(value))
        }
    }

    // 3. Find the mapOf(vararg pairs: Pair<K,V>): Map<K,V> function
    // You can choose between "mapOf" for an immutable Map or "mutableMapOf" for a MutableMap.
    // Let's use "mapOf" as an example.
    val mapFactoryFqName = CallableId(FqName("kotlin.collections"), Name.identifier("mapOf"))
    //val mapFactoryFqName = FqName("kotlin.collections.mapOf")
    // If you need a MutableMap:
    // val mapFactoryFqName = FqName("kotlin.collections.mutableMapOf")

    val mapFactorySymbol = pluginContext.referenceFunctions(mapFactoryFqName).firstOrNull { symbol ->
        val function = symbol.owner
        // Check for signature: fun <K, V> mapOf(vararg pairs: Pair<K, V>): Map<K, V>
        if (function.typeParameters.size != 2) return@firstOrNull false // Needs K, V

        val valueParameters = function.valueParameters
        if (valueParameters.size != 1) return@firstOrNull false
        val varargParameter = valueParameters[0]
        if (!varargParameter.isVararg) return@firstOrNull false

        val varargElementType = varargParameter.type.getVarargElementType(context)
        val varargElementTypeAsSimpleType = varargElementType as? IrSimpleType ?: return@firstOrNull false

        // Check if vararg element is Pair<K, V> where K, V are function's type parameters
        val classifierOK = varargElementTypeAsSimpleType.classifier == pairClassSymbol
//        val typeArgsOK = varargElementTypeAsSimpleType.arguments.size == 2 &&
//                (varargElementTypeAsSimpleType.arguments[0].typeOrNull as? IrTypeParameterType)?.classifier?.owner == function.typeParameters[0] &&
//                (varargElementTypeAsSimpleType.arguments[1].typeOrNull as? IrTypeParameterType)?.classifier?.owner == function.typeParameters[1]

        val typeArgsOK = true

        classifierOK && typeArgsOK
    } ?: error("Suitable '$mapFactoryFqName' function (one with vararg Pair<K,V>) not found.")

    // 4. Determine the type for IrVarargImpl
    // This is Array<out Pair<String, String>>
    // We get the generic vararg parameter type (Array<out Pair<K,V>>) and substitute K,V with String
    val genericVarargParamType = mapFactorySymbol.owner.valueParameters[0].type
    //val typeSubstitutor = ಲ್ಲ // Assuming IrTypeSubstitutor is available or map directly
    val typeSubstitutionMap = mapOf(
        mapFactorySymbol.owner.typeParameters[0].symbol to stringType,
        mapFactorySymbol.owner.typeParameters[1].symbol to stringType
    )
    val specificVarargType = genericVarargParamType.substitute(typeSubstitutionMap)


    // 5. Call the mapOf function with the pairs
    return irCall(mapFactorySymbol).apply {
        putTypeArgument(0, stringType) // Substitute K -> String for mapOf<K, V>
        putTypeArgument(1, stringType) // Substitute V -> String for mapOf<K, V>
        putValueArgument(
            0,
            IrVarargImpl(
                startOffset = this@irStringStringMap.startOffset,
                endOffset = this@irStringStringMap.endOffset,
                type = specificVarargType,           // Type: Array<out Pair<String, String>>
                varargElementType = pairStringStringType, // Element Type: Pair<String, String>
                elements = irPairExpressions
            )
        )
    }
}

// Helper to get vararg element type, usually present in IrUtils or can be defined
internal fun IrType.getVarargElementType(context: IrGeneratorContext): IrType {
    require(this is IrSimpleType && this.classifier == context.irBuiltIns.arrayClass) {
        "Vararg parameter must be of array type"
    }
    return this.arguments.single().typeOrNull ?: error("Array type for vararg has no type argument")
}