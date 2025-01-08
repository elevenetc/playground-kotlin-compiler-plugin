# Kotlin compiler plugins playground

This repository contains basic examples demonstrating the use of Kotlin compiler plugins for both backend and frontend development.

- Add println call to class method: [implementation](compiler-plugin/src/main/kotlin/addPrint), [tests](compiler-plugin/src/test/kotlin/AddPrintPluginTest.kt)
- Add property to class: [implementation](compiler-plugin/src/main/kotlin/addProperty), [tests](compiler-plugin/src/test/kotlin/AddPropertyPluginTest.kt)
- Add method to class: [implementation](compiler-plugin/src/main/kotlin/addMethod), [tests](compiler-plugin/src/test/kotlin/AddMethodPluginTest.kt)

## Test
* Run `./gradlew check` to run all checks, including tests.