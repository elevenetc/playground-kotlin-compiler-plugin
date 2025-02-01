package utils

private const val green = "\u001B[32m"
private const val yellow = "\u001B[33m"
private const val blue = "\u001B[34m"
private const val reset = "\u001B[0m"

fun printlnCode(value: String, fileName: String = "") {
    printFileName(fileName)
    println(buildPrintlnCode(value, fileName))
}

fun printFileName(fileName: String) {
    if (fileName.isNotEmpty()) println("${yellow}$fileName${reset}\n")
}

fun buildPrintlnCode(value: String, fileName: String = ""): String {
    return buildString {
        var lineId = 0
        appendLine(value.lines().joinToString(separator = "") { line ->
            (lineId++).toString() + ": " + "${green}${line.addSyntaxStyling()}${reset}\n"
        })
    }
}

private fun String.addSyntaxStyling(): String {
    return replace("{", "${blue}{${green}")
        .replace("}", "${blue}}${green}")
        .replace("(", "${yellow}(${green}")
        .replace(")", "${yellow})${green}")
        .replace("class", "${blue}class${green}")
        .replace("public", "${blue}public${green}")
        .replace("private", "${blue}private${green}")
        .replace("new", "${blue}new${green}")
        .replace("return", "${blue}return${green}")
}