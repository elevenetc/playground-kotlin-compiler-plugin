package utils.decompile

fun String.dropCfrComment(): String {
    val result = StringBuilder()
    var inCommentBlock = false

    for (line in this.lineSequence()) {
        val trimmedLine = line.trim()
        when {
            trimmedLine.startsWith("/*") -> inCommentBlock = true
            trimmedLine.endsWith("*/") -> inCommentBlock = false
            !inCommentBlock -> result.appendLine(line)
        }
    }
    return result.toString().dropLast(1) //drop last new line
}