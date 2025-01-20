import org.jetbrains.kotlin.CallLogger
import kotlin.test.Test
import kotlin.uuid.ExperimentalUuidApi

class DetachedCallTest {
    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun test() {
//        val a = CallLogger.instance.start("a")
//        val b = CallLogger.instance.start("b")
//        CallLogger.instance.end(b)
//        CallLogger.instance.end(a)

        CallLogger.instance.end(CallLogger.instance.start("a"))
        CallLogger.instance.end(CallLogger.instance.start("b"))

        println(CallLogger.instance.dumpToString())
    }
}