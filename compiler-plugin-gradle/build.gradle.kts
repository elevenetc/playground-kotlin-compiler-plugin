plugins {
    kotlin("jvm") version "2.0.21"
    id("java-gradle-plugin")
    alias(libs.plugins.buildConfig)
    id("maven-publish")
    kotlin("kapt") version "2.0.21"
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(libs.kotlin.gradlePlugin.api)

    compileOnly(libs.kotlin.compilerEmbeddable)
    compileOnly(libs.kotlin.stdlib)

    kapt("com.google.auto.service:auto-service:1.1.1")
    compileOnly("com.google.auto.service:auto-service-annotations:1.1.1")

    testImplementation(kotlin("test"))
    testImplementation(libs.kotlin.stdlib)
    testImplementation(libs.kotlin.reflect)
    testImplementation(libs.kotlin.compilerEmbeddable)
    testImplementation(libs.kotlin.compilerTestFramework)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.junit)
    testImplementation(libs.kotlin.compileTesting)
    testImplementation(libs.cfr)
    testImplementation("org.jetbrains.kotlin:call-logger:0.0.1")
}

//buildConfig {
//    packageName("org.jetbrains.kotlin")
//
//    buildConfigField("String", "KOTLIN_PLUGIN_ID", "\"playground.compiler.plugin\"")
//    buildConfigField("String", "KOTLIN_PLUGIN_GROUP", "\"${project.group}\"")
//    buildConfigField("String", "KOTLIN_PLUGIN_NAME", "\"${project.name}\"")
//    buildConfigField("String", "KOTLIN_PLUGIN_VERSION", "\"${project.version}\"")
//}

kotlin {
    jvmToolchain(20)
}

gradlePlugin {
    plugins {
        create("playgroundCompilerPlugin") {
            id = "playground.compiler.plugin"
            group = "org.jetbrains.kotlin"
            version = "0.0.1"
            implementationClass = "org.jetbrains.kotlin.PlaygroundGradleSupportPlugin"
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "playground.compiler.plugin"
            groupId = "org.jetbrains.kotlin"
            version = "0.0.1"

            //artifact(project(":compiler-plugin").tasks.getByName("jar"))
        }
    }

    repositories {
        maven {
            url = uri("${System.getProperty("user.home")}/.m2/repository")
        }
    }
}