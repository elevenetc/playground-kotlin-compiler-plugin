import org.jetbrains.kotlin.InstanceLogger
import kotlin.test.Test
import kotlin.test.assertEquals

class InstanceLoggerTest {
    @Test
    fun `test logger`() {
        val foo = Foo()
        foo.call_0()
        foo.call_1("42")
        foo.call_2()

        val log = InstanceLogger.instance.getLog()["Foo"] ?: error("log is empty")

        assert(log.size == 3)
        assertEquals("call_0", log[0].methodName)
        assertEquals("call_1", log[1].methodName)
        assertEquals("call_2", log[2].methodName)

        assertEquals("42", log[1].params["param0"])

        assertEquals("42", log[2].returnValue)
    }
}

class Foo {
    fun call_0() = Unit
    fun call_1(param0: String) = Unit
    fun call_2(): Int = 42
}

class ApiService {
    fun getData(): String {
        return loadData(10)
    }

    fun loadData(time: Long): String {
        if (time % 2 == 0L) throw Error("flaky crash")
        return ""
    }
}