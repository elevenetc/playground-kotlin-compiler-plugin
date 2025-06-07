package utils

@Suppress("UNCHECKED_CAST")
fun <T> ClassLoader.loadClass(clazz: Class<T>): Class<T> {
    return loadClass(clazz.typeName) as? Class<T> ?: error("Failed to load class ${clazz.typeName}")
}