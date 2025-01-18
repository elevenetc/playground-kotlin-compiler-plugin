import org.junit.Test
import utils.decompile.dropKotlinMetadata
import kotlin.test.assertEquals

class DropKotlinMetadataTest {
    @Test
    fun `test - kotlin Metadata is dropped`() {
        val source = """
            import kotlin.Metadata;
            @Metadata(mv={2, 1, 0}, k=1, xi=48, d1={"\u0000\u0010\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u0002\u0018\u00002\u00020\u0001B\u0007\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u0006\u0010\u0004\u001a\u00020\u0005"}, d2={"LFoo;", "", "<init>", "()V", "bar", ""})
            public final class Foo {
            }
        """.trimIndent()
        val expected = """
            public final class Foo {
            }
        """.trimIndent()

        assertEquals(expected, source.dropKotlinMetadata())
    }
}