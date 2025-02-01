plugins {
    kotlin("jvm") version libs.versions.kotlin
    id("java-gradle-plugin")
    alias(libs.plugins.buildConfig)
    id("maven-publish")
    kotlin("kapt") version libs.versions.kotlin
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(libs.kotlin.gradlePlugin.api)
    compileOnly(libs.kotlin.compilerEmbeddable)
    compileOnly(libs.kotlin.stdlib)
    compileOnly("com.google.auto.service:auto-service-annotations:1.1.1")

    kapt("com.google.auto.service:auto-service:1.1.1")

    implementation("org.jetbrains.kotlin:compiler-dependencies:0.0.1")

    testImplementation(kotlin("test"))
    testImplementation(libs.kotlin.stdlib)
    testImplementation(libs.kotlin.reflect)
    testImplementation(libs.kotlin.compilerEmbeddable)
    testImplementation(libs.kotlin.compilerTestFramework)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.junit)
    testImplementation(libs.kotlin.compileTesting)
    testImplementation(libs.cfr)

    testImplementation(gradleTestKit())
}

//buildConfig {
//    packageName("org.jetbrains.kotlin")
//
//    buildConfigField("String", "KOTLIN_PLUGIN_ID", "\"playground.compiler.plugin\"")
//    buildConfigField("String", "KOTLIN_PLUGIN_GROUP", "\"${project.group}\"")
//    buildConfigField("String", "KOTLIN_PLUGIN_NAME", "\"${project.name}\"")
//    buildConfigField("String", "KOTLIN_PLUGIN_VERSION", "\"${project.version}\"")
//}

val pluginGroupId = "org.jetbrains.kotlin"
val pluginId = "compiler-plugin"
val pluginVersion = "0.0.1"
version = pluginVersion
group = pluginGroupId

kotlin {
    jvmToolchain(20)
}

gradlePlugin {
    plugins {
        create("playgroundCompilerPlugin") {
            id = pluginId
            group = pluginGroupId
            version = pluginVersion
            implementationClass = "org.jetbrains.kotlin.PlaygroundGradleSupportPlugin"
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = pluginId
            groupId = pluginGroupId
            version = pluginVersion
        }
    }

    repositories {
        maven {
            url = uri("${System.getProperty("user.home")}/.m2/repository")
        }
    }
}