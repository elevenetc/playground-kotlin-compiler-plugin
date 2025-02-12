import org.junit.Test
import utils.decompile.dropCfrComment
import kotlin.test.assertEquals

class CfrUtilsTest {
    @Test
    fun `test - drop crf comment block`() {
        val inputSource = """
            |/*
            |* Decompiled with CFR 0.152.
            | */
            |public final class Foo {
            |}
        """.trimMargin()

        val expected = """
            |public final class Foo {
            |}
        """.trimMargin()
        assertEquals(expected, inputSource.trimIndent().dropCfrComment())
    }

    @Test
    fun `test drop crf extended comment block`() {
        val inputSource = """
            |/*
            | * Decompiled with CFR 0.152.
            | * 
            | * Could not load the following classes:
            | *  addCallLog.CallLogger
            | */
            |public final class Foo {
            |}
        """.trimMargin()
        val expected = """
            |public final class Foo {
            |}
        """.trimMargin()
        assertEquals(expected, inputSource.trimIndent().dropCfrComment())
    }
}