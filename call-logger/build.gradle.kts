plugins {
    kotlin("jvm") version "2.0.21"
    id("java-library")
}

group = "org.jetbrains.kotlin"
version = "0.0.1"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(20)
}
dependencies {
    testImplementation(kotlin("test"))
}