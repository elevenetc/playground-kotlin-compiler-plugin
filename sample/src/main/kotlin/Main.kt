import org.jetbrains.kotlin.CallLogger
import org.jetbrains.kotlin.IgnoreCallLog

fun main() {
    val foo = Foo()
    foo.bar()
    println(CallLogger.instance.dumpToString())
}

class Foo {
    fun bar() {
        privateFoo()
    }

    @IgnoreCallLog
    private fun privateFoo() {

    }
}