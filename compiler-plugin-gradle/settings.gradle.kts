rootProject.name = "playground-compiler-plugin"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

//includeBuild("../compiler-plugin") {
//    dependencySubstitution {
//        substitute(module("org.jetbrains.kotlin:compiler-plugin"))
//            .using(project(":compiler-plugin"))
//    }
//}