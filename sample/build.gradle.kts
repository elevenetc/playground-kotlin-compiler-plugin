plugins {
    kotlin("jvm") version "2.0.21"
    id("compiler-plugin").version("0.0.1")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:call-logger:0.0.1")
}

group = "org.jetbrains.kotlin"
version = "unspecified"

kotlin {
    jvmToolchain(20)
}