package utils.decompile

import utils.dropLine

fun String.dropCfrComment(): String {
    return if (this.trim().startsWith("/*")) dropLine(3) else this
}