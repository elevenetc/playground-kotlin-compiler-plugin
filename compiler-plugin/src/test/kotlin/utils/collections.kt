package utils

fun <T> Iterable<T>.second(): T {
    when (this) {
        is List -> return this[1]
        else -> {
            val iterator = iterator()
            if (!iterator.hasNext()) throw NoSuchElementException("Collection is empty.")
            iterator.next()
            if (!iterator.hasNext()) throw NoSuchElementException("Collection has 1 item, expected at least 2.")
            return iterator.next()
        }
    }
}