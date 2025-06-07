package org.jetbrains.kotlin

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.addCallLog.AddCallLogCommandLineProcessor.Companion.ENABLE_CALLS_TRACING
import org.jetbrains.kotlin.addCallLog.AddCallLogCommandLineProcessor.Companion.ENABLE_CLASS_TRACING
import org.jetbrains.kotlin.addCallLog.AddCallLogCommandLineProcessor.Companion.EXCLUDED_FILES
import org.jetbrains.kotlin.addCallLog.AddCallLogCommandLineProcessor.Companion.EXCLUDED_FQN
import org.jetbrains.kotlin.addCallLog.AddCallLogCommandLineProcessor.Companion.TRACE_CLASS
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

class PlaygroundGradleSupportPlugin : KotlinCompilerPluginSupportPlugin {

    override fun apply(target: Project) {
        target.extensions.create(
            "playgroundCompilerPluginSettings",
            PlaygroundCompilerPluginSettingsExtension::class.java
        )
    }

    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project
        val extension = project.extensions.getByType(PlaygroundCompilerPluginSettingsExtension::class.java)

        val enabledCallsTracing = extension.enabledCallsTracing.get()
        val excludedFqns = extension.excludedCallsFqns.get()
        val excludedFiles = extension.excludedCallsFiles.get()

        val enabledClassTracing = extension.enabledClassTracing.get()
        val traceClassesFqns = extension.traceClassesFqns.get()

        val excludedFqnsOptions = excludedFqns.map { fqn ->
            SubpluginOption(
                key = EXCLUDED_FQN.value,
                value = fqn
            )
        }

        val excludedFilesOptions = excludedFiles.map { file ->
            SubpluginOption(
                key = EXCLUDED_FILES.value,
                value = file
            )
        }

        val traceClassesFqnsOptions = traceClassesFqns.map { fqn ->
            SubpluginOption(
                key = TRACE_CLASS.value,
                value = fqn
            )
        }

        val options = listOf(
            SubpluginOption(
                key = ENABLE_CALLS_TRACING.value,
                value = enabledCallsTracing.toString()
            ),
            SubpluginOption(
                key = ENABLE_CLASS_TRACING.value,
                value = enabledClassTracing.toString()
            )
        ) + excludedFqnsOptions + excludedFilesOptions + traceClassesFqnsOptions

        println("OPTS: ${options.map { it.key + "=" + it.value }}")

        return project.provider {
            options
        }
    }

    override fun getCompilerPluginId(): String = "playground.compiler.plugin.compiler"

    override fun getPluginArtifact(): SubpluginArtifact {
        return SubpluginArtifact(
            groupId = "org.jetbrains.kotlin",
            artifactId = "compiler-plugin",
            version = "0.0.1",
        )
    }

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true
}