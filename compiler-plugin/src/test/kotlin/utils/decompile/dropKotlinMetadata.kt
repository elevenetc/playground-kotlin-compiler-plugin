package utils.decompile

fun String.dropKotlinMetadata(): String {
    return lines().mapNotNull { line ->
        val trimmed = line.trim()
        if (trimmed.startsWith("import kotlin.Metadata") || trimmed.startsWith("@Metadata")) {
            null
        } else {
            line
        }
    }.joinToString("\n")
}