plugins {
    kotlin("jvm") version "2.0.21"
    id("java-gradle-plugin")
    alias(libs.plugins.buildConfig)
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(libs.kotlin.gradlePlugin.api)
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.0")
    testImplementation(kotlin("test"))
}

buildConfig {
    packageName("org.jetbrains.kotlin")

    buildConfigField("String", "KOTLIN_PLUGIN_ID", "\"playground.compiler.plugin\"")
    buildConfigField("String", "KOTLIN_PLUGIN_GROUP", "\"${project.group}\"")
    buildConfigField("String", "KOTLIN_PLUGIN_NAME", "\"${project.name}\"")
    buildConfigField("String", "KOTLIN_PLUGIN_VERSION", "\"${project.version}\"")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(20)
}

gradlePlugin {
    plugins {
        create("playgroundCompilerPlugin") {
            id = "compiler-plugin"
            group = "org.jetbrains.kotlin"
            version = "0.0.1"
            implementationClass = "org.jetbrains.kotlin.PlaygroundGradleSupportPlugin"
        }
    }
}