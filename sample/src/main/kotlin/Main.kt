import org.jetbrains.kotlin.CallLogger
import kotlin.concurrent.thread

fun main() {
    val foo = Foo()

    thread(name = "a") {
        foo.threadA()
    }.join()

    thread(name = "b") {
        foo.threadB()
    }.join()
    
    println(CallLogger.instance.dumpToString())
}

class Foo {


    fun threadA() {

    }

    fun threadB() {

    }
}