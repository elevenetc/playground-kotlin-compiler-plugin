package utils

fun <T> List<T>.firstOrError(): T {
    if (this.isEmpty()) error("List is empty, expected 1 element")
    else if (this.size > 1) error("List has more than 1 element, expected 1 element")
    return this[0]
}