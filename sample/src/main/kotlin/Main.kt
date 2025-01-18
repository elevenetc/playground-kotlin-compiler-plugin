import org.jetbrains.kotlin.CallLogger
import kotlin.uuid.ExperimentalUuidApi


@OptIn(ExperimentalUuidApi::class)
fun main() {
    val foo = Foo()
    foo.bar()
    println("hello world: ${CallLogger.instance.calls.size}")
}

class Foo {
    fun bar() {
        privateFoo()
    }

    private fun privateFoo() {

    }
}