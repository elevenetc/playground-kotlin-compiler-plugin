# Kotlin compiler plugins playground

This repository contains basic examples demonstrating the use of Kotlin compiler plugins for both backend and frontend development.

- Add println call to class
  method: [implementation](compiler-plugin/src/main/kotlin/org/jetbrains/kotlin/addPrint), [tests](compiler-plugin/src/test/kotlin/AddPrintPluginTest.kt)
- Add property to
  class: [implementation](compiler-plugin/src/main/kotlin/org/jetbrains/kotlin/addProperty), [tests](compiler-plugin/src/test/kotlin/AddPropertyPluginTest.kt)
- Add method to
  class: [implementation](compiler-plugin/src/main/kotlin/org/jetbrains/kotlin/addMethod), [tests](compiler-plugin/src/test/kotlin/AddMethodPluginTest.kt)
- Add argument to
  method: [implementation](compiler-plugin/src/main/kotlin/org/jetbrains/kotlin/addMethodArgument), [tests](compiler-plugin/src/test/kotlin/AddMethodArgumentPluginTest.kt)
- Add static call to a
  method: [implementation](compiler-plugin/src/main/kotlin/org/jetbrains/kotlin/addDependencyCallToMethod), [tests](compiler-plugin/src/test/kotlin/AddDependencyCallToMethodPluginTest.kt)
- Add call
  logger: [implementation](compiler-plugin/src/main/kotlin/org/jetbrains/kotlin/addCallLog), [tests](compiler-plugin/src/test/kotlin/AddCallLogPluginTest.kt)

## Test

* Run `./gradlew runChecks` to run all checks, including tests.

## Development

### Use sample project

See [sample](sample)

### Use with external project

1. Build and publish plugin to local maven repository `./gradlew buildAndPublish`
2. Use it

```kotlin
//settings.gradle.kts
pluginManagement {
  repositories {
    mavenLocal()
  }
}
//build.gradle.kts
plugins {
  id("compiler-plugin") version "0.0.1"
}
repositories {
  mavenLocal()
}
dependencies {
  implementation("org.jetbrains.kotlin:compiler-dependencies:0.0.1")
}
```