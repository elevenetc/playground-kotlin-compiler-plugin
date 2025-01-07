plugins {
    kotlin("jvm")
}

group = "org.jetbrains.kotlin"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(libs.kotlin.compilerEmbeddable)
    compileOnly(libs.kotlin.stdlib)

    testImplementation(kotlin("test"))
    testImplementation(libs.kotlin.stdlib)
    testImplementation(libs.kotlin.compilerEmbeddable)
    testImplementation(libs.kotlin.compilerTestFramework)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.junit)
    testImplementation(libs.kotlin.compileTesting)
    testImplementation(libs.cfr)
}

/*
Dependencies visible to the compiler used inside the tests `Compiler.compile`
 */
val testCompilerDependencies = configurations.create("testCompilerDependencies") {
    isCanBeConsumed = false
    isCanBeResolved = true
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.JAR))
    }
}

tasks.withType<Test>().configureEach {
    dependsOn(testCompilerDependencies)
    val testCompilerClasspath = testCompilerDependencies.files

    doFirst {
        systemProperty(
            "testCompilerClasspath",
            testCompilerClasspath.joinToString(File.pathSeparator) { it.absolutePath })
    }
}