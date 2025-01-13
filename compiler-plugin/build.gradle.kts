plugins {
    kotlin("jvm") version "2.0.21"
    kotlin("kapt") version "2.0.21"
}

group = "org.jetbrains.kotlin"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
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

kotlin {
    jvmToolchain(20)
}