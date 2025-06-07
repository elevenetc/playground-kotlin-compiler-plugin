plugins {
    kotlin("jvm") version libs.versions.kotlin
    id("application")
    id("compiler-plugin").version("0.0.1")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:compiler-dependencies:0.0.1")
    testImplementation("org.jetbrains.kotlin:compiler-dependencies:0.0.1")
    testImplementation(kotlin("test"))
}

group = "org.jetbrains.kotlin"
version = "unspecified"

kotlin {
    jvmToolchain(8)
}

playgroundCompilerPluginSettings {
    enabledClassTracing = true
    enabledCallsTracing = true
    traceClassesFqns = listOf(
        "ViewModel", "Foo"
    )
}