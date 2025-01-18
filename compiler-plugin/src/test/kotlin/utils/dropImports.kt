package utils

fun String.dropImports(): String {
    return lines()
        .filter { !it.startsWith("import") }
        .joinToString("\n")
}