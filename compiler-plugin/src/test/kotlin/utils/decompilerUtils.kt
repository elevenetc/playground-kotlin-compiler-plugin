package utils

import com.tschuchort.compiletesting.JvmCompilationResult
import org.benf.cfr.reader.api.CfrDriver
import org.benf.cfr.reader.api.OutputSinkFactory
import org.benf.cfr.reader.api.OutputSinkFactory.SinkClass
import org.benf.cfr.reader.api.OutputSinkFactory.SinkType
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import utils.decompile.dropCfrComment
import utils.decompile.dropKotlinMetadata


@OptIn(ExperimentalCompilerApi::class)
fun JvmCompilationResult.decompileClassAndTrim(): String {
    val clazz = this.generatedFiles.filter { it.name.endsWith(".class") }.firstOrError()
    val decompiled = decompileClass(clazz.absolutePath)
    return decompiled.dropCfrComment().dropKotlinMetadata().dropEmptyLines()
}

fun decompileClass(classFilePath: String, options: Map<String, String> = emptyMap()): String {
    val result = StringBuilder()
    val cfrDriver = CfrDriver.Builder()
        .withOutputSink(createOutputSink(result))
        .withOptions(options)
        .build()
    cfrDriver.analyse(listOf(classFilePath))
    return result.toString()
}

fun createOutputSink(result: StringBuilder): OutputSinkFactory {

    return object : OutputSinkFactory {
        override fun <T : Any?> getSink(sinkType: SinkType, sinkClass: SinkClass): OutputSinkFactory.Sink<T>? {
            return if (sinkType == SinkType.JAVA) {
                OutputSinkFactory.Sink { content ->
                    result.append(content)
                }
            } else {
                OutputSinkFactory.Sink { content ->
                    //TODO: add logs collection
                }
            }
        }

        override fun getSupportedSinks(sinkType: SinkType, collection: Collection<SinkClass>): List<SinkClass> {
            return listOf(SinkClass.STRING)
        }
    }
}