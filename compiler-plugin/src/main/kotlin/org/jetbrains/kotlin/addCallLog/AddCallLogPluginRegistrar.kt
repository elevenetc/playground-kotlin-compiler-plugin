package org.jetbrains.kotlin.addCallLog

import com.google.auto.service.AutoService
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration

@OptIn(ExperimentalCompilerApi::class)
@AutoService(CompilerPluginRegistrar::class)
class AddCallLogPluginRegistrar : CompilerPluginRegistrar() {
    override val supportsK2: Boolean
        get() = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {

        val enabledCallsTracing = configuration.get(AddCallLogCommandLineProcessor.ENABLE_CALLS_TRACING.key) ?: false
        val enabledClassTracing = configuration.get(AddCallLogCommandLineProcessor.ENABLE_CLASS_TRACING.key) ?: false

        val rawFqns = configuration.get(AddCallLogCommandLineProcessor.EXCLUDED_FQN.key) ?: ""
        val fqns = rawFqns.split("|")

        val rawFiles = configuration.get(AddCallLogCommandLineProcessor.EXCLUDED_FILES.key) ?: ""
        val files = rawFiles.split("|")

        val rawTracesClassesFqns = configuration.get(AddCallLogCommandLineProcessor.TRACE_CLASS.key) ?: ""
        val tracesClassesFqns = rawTracesClassesFqns.split("|")

        //throw Error(">")

        IrGenerationExtension.registerExtension(
            AddCallLogPluginExtension(
                enabledCallsTracing,
                enabledClassTracing,
                fqns,
                files,
                tracesClassesFqns
            )
        )
    }
}