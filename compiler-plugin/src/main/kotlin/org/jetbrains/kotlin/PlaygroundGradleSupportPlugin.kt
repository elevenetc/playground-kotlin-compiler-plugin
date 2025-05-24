package org.jetbrains.kotlin

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.addCallLog.AddCallLogCommandLineProcessor
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

        val enabled = extension.enabled.get()
        val excludedFqns = extension.excludedFqns.get()
        val excludedFiles = extension.excludedFiles.get()
        val traceClassesFqns = extension.traceClassesFqns.get()

        val excludedFqnsOptions = excludedFqns.map { fqn ->
            SubpluginOption(
                key = AddCallLogCommandLineProcessor.EXCLUDED_FQN.value,
                value = fqn
            )
        }

        val excludedFilesOptions = excludedFiles.map { file ->
            SubpluginOption(
                key = AddCallLogCommandLineProcessor.EXCLUDED_FILES.value,
                value = file
            )
        }

        val traceClassesFqnsOptions = traceClassesFqns.map { fqn ->
            SubpluginOption(
                key = AddCallLogCommandLineProcessor.TRACE_CLASS.value,
                value = fqn
            )
        }

        return project.provider {
            listOf(
                SubpluginOption(key = AddCallLogCommandLineProcessor.ENABLE.value, value = enabled.toString()),
            ) + excludedFqnsOptions + excludedFilesOptions + traceClassesFqnsOptions
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