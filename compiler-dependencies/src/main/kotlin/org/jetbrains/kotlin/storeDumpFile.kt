package org.jetbrains.kotlin

import java.io.File
import java.text.SimpleDateFormat
import java.util.*

private const val dumpsDir = "build/kcrif"

internal fun storeDump(dump: String, subDir: String, filePrefix: String) {
    //TODO: replace with multiplatform implementation

    val dir = "$dumpsDir/$subDir"
    File(dir).mkdirs()
    File("$dir/$filePrefix-${getCurrentReadableTime()}.json").writeText(dump)
}

private fun getCurrentReadableTime(): String {
    val currentMillis = System.currentTimeMillis()
    val date = Date(currentMillis)
    val format = SimpleDateFormat("yyyy-MM-dd-HH:mm:ss", Locale.getDefault())
    return format.format(date)
}