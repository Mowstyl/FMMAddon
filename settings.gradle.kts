plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version ("0.9.0")
}

gradle.extra["projectName"] = "FMMAddon"
rootProject.name = gradle.extra["projectName"].toString().lowercase()
