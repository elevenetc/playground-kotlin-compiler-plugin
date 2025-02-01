import org.junit.Test
import utils.assertEqualsCode

class AssertEqualsCodeTests {
    @Test
    fun `test - simple equality`() {
        val foo = """
            val a = 10
        """.trimIndent()
        val bar = """
            val a = 10
        """.trimIndent()

        assertEqualsCode(foo, bar)
    }

    @Test(expected = AssertionError::class)
    fun `test - simple inequality`() {
        val foo = """
            val a = 0
            fun foo() = Unit
            val s = "s"
        """.trimIndent()
        val bar = """
            val a = 1
            fun foo() = Unit
            val s = "x"
        """.trimIndent()

        assertEqualsCode(foo, bar)
    }
}