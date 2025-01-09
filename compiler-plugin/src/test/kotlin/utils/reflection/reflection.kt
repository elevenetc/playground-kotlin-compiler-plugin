package utils.reflection

import kotlin.reflect.KClass
import kotlin.reflect.full.*

fun Any.get(propertyName: String): Any? {
    val kClass = this::class
    val property = kClass.declaredMemberProperties.find { it.name == propertyName }
        ?: throw NoSuchElementException("Property '$propertyName' not found in class ${kClass.simpleName}.")
    return property.call(this)
}

fun Any.call(methodName: String): Any? {
    val kClass = this::class
    val method = kClass.declaredFunctions.find { it.name == methodName }
        ?: throw NoSuchElementException("Method '$methodName' not found in class ${kClass.simpleName}.")
    return method.callBy(mapOf(method.instanceParameter!! to this))
}

fun KClass<*>.newInstance(): Any {
    return primaryConstructor?.call() ?: newInstance()
}

fun KClass<*>.getCompanionProperty(name: String): Any {
    return (companionObjectInstance ?: error("No companion")).let { companion ->
        companion.get(name) ?: error("Companion property `$name` is null")
    }
}