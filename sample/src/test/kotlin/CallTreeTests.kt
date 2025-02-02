import org.jetbrains.kotlin.CallLogger
import kotlin.test.Test
import kotlin.test.assertEquals

class CallTreeTests {

    @Test
    fun `test - lambda argument`() {
        val logger = CallLogger.instance

        class Foo {
            fun bar(f: () -> Unit) {
                f()
            }
        }

        val foo = Foo()
        foo.bar {

        }

        val currentCall = logger.threads.values.first().currentCall
        assertEquals("CallTreeTests.test - lambda argument", currentCall?.fqn ?: error("current call is null"))
        assertEquals(1, currentCall.children.size)
        assertEquals("CallTreeTests.test - lambda argument.Foo.bar", currentCall.children[0].fqn)
    }

    @Test
    fun `test - nested call`() {

        fun c() {

        }

        fun b() {
            c()
        }

        fun a() {
            b()
        }

        a()

        println(CallLogger.instance)


    }

}