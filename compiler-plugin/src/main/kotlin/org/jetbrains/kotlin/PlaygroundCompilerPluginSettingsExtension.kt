package org.jetbrains.kotlin

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

abstract class PlaygroundCompilerPluginSettingsExtension @Inject constructor(objects: ObjectFactory) {
    val enabledCallsTracing: Property<Boolean> =
        objects.property(Boolean::class.javaObjectType).convention(false)

    val excludedCallsFqns = objects.listProperty(String::class.java).convention(emptyList())
    val excludedCallsFiles = objects.listProperty(String::class.java).convention(emptyList())

    val enabledClassTracing: Property<Boolean> =
        objects.property(Boolean::class.javaObjectType).convention(false)

    val traceClassesFqns = objects.listProperty(String::class.java).convention(emptyList())
}