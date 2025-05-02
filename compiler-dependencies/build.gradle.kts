plugins {
    kotlin("jvm") version libs.versions.kotlin
    kotlin("plugin.serialization") version libs.versions.kotlin
    id("java-library")
    id("maven-publish")
}

val libraryGroupId = "org.jetbrains.kotlin"
val libraryId = "compiler-dependencies"
val libraryVersion = "0.0.1"
version = libraryVersion
group = libraryGroupId

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlinxSerialization)
    testImplementation(kotlin("test"))
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = libraryId
            groupId = libraryGroupId
            version = libraryVersion
        }
    }

    repositories {
        maven {
            url = uri("${System.getProperty("user.home")}/.m2/repository")
        }
    }
}

kotlin {
    jvmToolchain(8)
}