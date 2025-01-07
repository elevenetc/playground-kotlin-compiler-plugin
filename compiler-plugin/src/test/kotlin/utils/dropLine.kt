package utils

fun String.dropLine(n: Int): String {
    return this.lineSequence()
        .drop(n)
        .joinToString("\n")
}