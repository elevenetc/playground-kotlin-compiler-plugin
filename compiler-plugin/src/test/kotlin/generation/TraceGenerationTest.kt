package generation

import org.jetbrains.kotlin.addCallLog.AddCallLogCommandLineProcessor
import org.jetbrains.kotlin.addCallLog.AddCallLogCommandLineProcessor.Companion.ENABLE_CLASS_TRACING
import org.jetbrains.kotlin.addCallLog.AddCallLogCommandLineProcessor.Companion.TRACE_CLASS
import org.jetbrains.kotlin.addCallLog.AddCallLogPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import utils.*

@OptIn(ExperimentalCompilerApi::class)
class TraceGenerationTest {

    @Rule
    @JvmField
    var tempDir = TemporaryFolder()

    @Test
    fun `test - no parameters, no return value`() {
        val source = """
            class Foo {
                fun bar() = Unit
            }
        """.trimIndent()

        assertEqualsCode(
            """
            public final class Foo {
                public final void bar() {
                    String string = InstanceLogger.start${'$'}default(InstanceLogger.Companion.getInstance(), "Foo", "bar", null, 4, null);
                    InstanceLogger.end${'$'}default(InstanceLogger.Companion.getInstance(), "Foo", string, null, 4, null);
                }
            }
        """.trimIndent(), doTest(source)
        )
    }

    @Test
    fun `test - one simple type parameter`() {
        val source = """
            class Foo {
                fun bar(value: Int) = Unit
            }
        """.trimIndent()

        assertEqualsCode(
            """
            public final class Foo {
                public final void bar(int value) {
                    Pair[] pairArray = new Pair[]{new Pair<String, String>("value", String.valueOf(value))};
                    String string = InstanceLogger.Companion.getInstance().start("Foo", "bar", MapsKt.mapOf(pairArray));
                    InstanceLogger.end${'$'}default(InstanceLogger.Companion.getInstance(), "Foo", string, null, 4, null);
                }
            }
        """.trimIndent(),
            doTest(source)
        )
    }

    @Test
    fun `test - one complex type parameter`() {
        val source = """
            class Bar
            class Foo {
                fun bar(bar: Bar) = Unit
            }
        """.trimIndent()

        assertEqualsCode(
            """
            public final class Foo {
                public final void bar(@NotNull Bar bar) {
                    Intrinsics.checkNotNullParameter(bar, "bar");
                    Pair[] pairArray = new Pair[]{new Pair<String, String>("bar", bar.toString())};
                    String string = InstanceLogger.Companion.getInstance().start("Foo", "bar", MapsKt.mapOf(pairArray));
                    InstanceLogger.end${'$'}default(InstanceLogger.Companion.getInstance(), "Foo", string, null, 4, null);
                }
            }
        """.trimIndent(),
            doTest(source)
        )
    }

    @Test
    fun `test - return simple type`() {
        val source = """
            class Foo {
                fun bar(): Int {
                    return 42
                }
            }
        """.trimIndent()

        assertEqualsCode(
            """
            public final class Foo {
                public final int bar() {
                    String string = InstanceLogger.start${'$'}default(InstanceLogger.Companion.getInstance(), "Foo", "bar", null, 4, null);
                    InstanceLogger.Companion.getInstance().end("Foo", string, String.valueOf(42));
                    return 42;
                }
            }
        """.trimIndent(),
            doTest(source)
        )
    }

    @Test
    fun `test - return complex type`() {
        val source = """
            class Bar
            class Foo {
                fun bar(): Bar {
                    return Bar()
                }
            }
        """.trimIndent()

        assertEqualsCode(
            """
            public final class Foo {
                @NotNull
                public final Bar bar() {
                    String string = InstanceLogger.start${'$'}default(InstanceLogger.Companion.getInstance(), "Foo", "bar", null, 4, null);
                    Bar bar = new Bar();
                    InstanceLogger.Companion.getInstance().end("Foo", string, bar.toString());
                    return bar;
                }
            }
        """.trimIndent(),
            doTest(source)
        )
    }

    private fun doTest(source: String): String {
        val result = compile(
            sourceInfo = buildSourceInfo(tempDir, source),
            registrar = AddCallLogPluginRegistrar(),
            processors = mapOf(
                AddCallLogCommandLineProcessor() to listOf(
                    ENABLE_CLASS_TRACING.option to true,
                    TRACE_CLASS.option to "Foo"
                )
            )
        ).also { result -> result.assertSuccess() }
        return result.decompileClassAndTrim("Foo.class")
    }
}