package utils

fun assertEqualsCode(expected: String, actual: String) {
    if (expected != actual) {
        val expectedCode = buildPrintlnCode(expected, "expected")
        val actualCode = buildPrintlnCode(actual, "actual")
        val diff = LinkedHashMap<String, String>()
        val expectedLines = expectedCode.lines()
        val actualLines = actualCode.lines()
        val maxLines = maxOf(expectedLines.size, actualLines.size)

        for (i in 0 until maxLines) {
            val expectedLine = expectedLines.getOrNull(i)
            val actualLine = actualLines.getOrNull(i)
            if (expectedLine != actualLine) {
                diff[expectedLine ?: ""] = actualLine ?: ""
            }
        }

        if (diff.isEmpty()) return

        printFileName("Expected")
        println(expectedCode)

        printFileName("Actual")
        println(actualCode)

        printFileName("Diff")

        diff.forEach { (expectedLine, actualLine) ->
            println(" ${expectedLine.trim()} -> ${actualLine.trim()}")
        }

        throw AssertionError()
    }
}