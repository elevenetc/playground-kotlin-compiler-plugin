import org.jetbrains.kotlin.CallLogger

fun main() {
    foo()
    println(CallLogger.instance.dumpToString())
}


fun foo() {
    bar {
        return
    }
}

inline fun bar(lambda: () -> Unit) {
    lambda()
}