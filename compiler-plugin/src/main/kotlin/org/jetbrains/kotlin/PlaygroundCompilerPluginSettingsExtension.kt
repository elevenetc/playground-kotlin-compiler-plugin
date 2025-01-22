package org.jetbrains.kotlin

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

abstract class PlaygroundCompilerPluginSettingsExtension @Inject constructor(objects: ObjectFactory) {
    val enabled: Property<Boolean> =
        objects.property(Boolean::class.javaObjectType).convention(true)

    val excludedFqns = objects.listProperty(String::class.java).convention(emptyList())
}