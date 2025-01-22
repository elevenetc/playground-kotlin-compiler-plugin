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

        return project.provider {
            listOf(
                SubpluginOption(key = AddCallLogCommandLineProcessor.ENABLE.value, value = enabled.toString()),
                SubpluginOption(
                    key = AddCallLogCommandLineProcessor.EXCLUDED_FQNS.value,
                    value = excludedFqns.joinToString(",")
                ),
            )
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