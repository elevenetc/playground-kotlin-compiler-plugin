import org.jetbrains.kotlin.CallLogger

fun main() {
    val foo = Foo()
    foo.bar()
    println(CallLogger.instance.dumpToString())
}

class Foo {
    fun bar() {
        privateFoo()
    }

    private fun privateFoo() {

    }
}