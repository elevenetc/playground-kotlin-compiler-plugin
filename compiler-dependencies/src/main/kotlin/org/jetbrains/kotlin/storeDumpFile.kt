package org.jetbrains.kotlin

import java.io.File
import java.text.SimpleDateFormat
import java.util.*

private const val dumpsDir = "build/call-logs"

internal fun storeDump(dump: String) {
    //TODO: replace with multiplatform implementation
    File(dumpsDir).mkdirs()
    File("$dumpsDir/call-logger-${getCurrentReadableTime()}.json").writeText(dump)
}

private fun getCurrentReadableTime(): String {
    val currentMillis = System.currentTimeMillis()
    val date = Date(currentMillis)
    val format = SimpleDateFormat("yyyy-MM-dd-HH:mm:ss", Locale.getDefault())
    return format.format(date)
}