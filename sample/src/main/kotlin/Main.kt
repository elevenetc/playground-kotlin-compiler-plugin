import org.jetrbains.kotlin.CallLogger

fun main() {
    val foo = Foo()
    foo.bar()
    println(CallLogger.instance)
}

class Foo {
    fun bar() {
        privateFoo()
    }

    private fun privateFoo() {

    }
}