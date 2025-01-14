import org.jetrbains.kotlin.CallLogger
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

        val currentCall = logger.currentCall
        assertEquals("CallTreeTests.test - lambda argument", currentCall?.fqn ?: error("current call is null"))
        assertEquals(1, currentCall.children.size)
        assertEquals("CallTreeTests.test - lambda argument.Foo.bar", currentCall.children[0].fqn)
        assertEquals("CallTreeTests.test - lambda argument.<anonymous>", currentCall.children[0].children[0].fqn)
    }

}