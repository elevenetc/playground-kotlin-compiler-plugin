import org.jetrbains.kotlin.CallLogger

fun main() {
    val foo = Foo()
    foo.bar()
    val logger = CallLogger.instance
    println(logger)
}

class Foo {
    fun bar() {
        privateFoo()
    }

    private fun privateFoo() {

    }
}