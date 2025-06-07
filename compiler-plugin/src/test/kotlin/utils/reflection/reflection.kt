package utils.reflection

import kotlin.reflect.KClass
import kotlin.reflect.full.*

@Suppress("UNCHECKED_CAST")
fun <T> Any.get(propertyName: String): T {
    val kClass = this::class
    val property = kClass.declaredMemberProperties.find { it.name == propertyName }
        ?: throw NoSuchElementException("Property '$propertyName' not found in class ${kClass.simpleName}.")
    return property.call(this) as T
}

fun Any.call(methodName: String): Any? {
    val kClass = this::class
    val method = kClass.declaredFunctions.find { it.name == methodName }
        ?: throw NoSuchElementException("Method '$methodName' not found in class ${kClass.simpleName}.")
    return method.callBy(mapOf(method.instanceParameter!! to this))
}

fun KClass<*>.newAnyInstance(): Any {
    return newInstance()
}

@Suppress("UNCHECKED_CAST")
fun <T> KClass<*>.newInstance(): T {
    return (primaryConstructor?.call() ?: newAnyInstance()) as T
}

fun <T> KClass<*>.getCompanionProperty(name: String): T {
    return (companionObjectInstance ?: error("No companion")).let { companion ->
        companion.get(name) ?: error("Companion property `$name` is null")
    }
}

fun KClass<*>.getAnyCompanionProperty(name: String): Any {
    return getCompanionProperty(name)
}