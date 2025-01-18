package utils

import kotlin.test.assertEquals
import kotlin.test.assertTrue

fun Map<*, *>.assertEmpty() {
    assertTrue(this.isEmpty(), "Collection is not empty")
}

fun Map<*, *>.assertSizeOf(size: Int) {
    assertEquals(size, this.size, "Expected size $size but found ${this.size}")
}