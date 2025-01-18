tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
    subprojects.forEach { delete(it.layout.buildDirectory) }
    gradle.includedBuilds.forEach { build -> delete(build.projectDir.resolve("build")) }
}

tasks.register("runChecks") {
    group = "verification"
    description = "Runs all checks (tests) on all subprojects."
    subprojects.forEach { subproject ->
        dependsOn(subproject.tasks.matching { it.name == "check" })
    }
    gradle.includedBuilds.forEach { includedBuild ->
        dependsOn(gradle.includedBuild(includedBuild.name).task(":check"))
    }
}

tasks.register("buildAndPublish") {
    group = "build"
    description = "Builds and publishes the compiler plugin and its dependencies."

    dependsOn(
        gradle.includedBuild("compiler-plugin").task(":build"),
        gradle.includedBuild("compiler-dependencies").task(":build"),
        gradle.includedBuild("compiler-plugin").task(":publish"),
        gradle.includedBuild("compiler-dependencies").task(":publish")
    )
}