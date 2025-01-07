package utils

fun String.dropEmptyLines(): String =
    this.lines()
        .filter { it.isNotBlank() }
        .joinToString("\n")