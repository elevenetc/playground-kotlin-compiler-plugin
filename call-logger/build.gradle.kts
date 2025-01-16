plugins {
    kotlin("jvm") version "2.0.21"
    id("java-library")
    id("maven-publish")
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

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = "org.jetbrains.kotlin"
            artifactId = "playground-compiler-plugin-logger"
            version = "0.0.1"
        }
    }

    repositories {
        maven {
            url = uri("${System.getProperty("user.home")}/.m2/repository")
        }
    }
}