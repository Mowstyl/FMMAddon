// import com.github.spotbugs.snom.Confidence
// import com.github.spotbugs.snom.Effort


plugins {
    `java-library`
    alias(libs.plugins.shadowPlugin)
    alias(libs.plugins.generatePOMPlugin)
    // alias(libs.plugins.spotBugsPlugin)
}


group = "com.clanjhoo"
version = "1.0.0"
description = "FreeMinecraftModels - MythicMobs compatibility addon"

ext.set("projectName", gradle.extra["projectName"].toString())
maven.pom {
    name = gradle.extra["projectName"].toString()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
        vendor = JvmVendorSpec.ORACLE
    }
}

repositories {
    gradlePluginPortal {
        content {
            includeGroup("com.gradleup")
            includeGroup("ru.vyarus")
            includeGroup("io.papermc.paperweight")
            includeGroup("com.github")
        }
    }
    maven {
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        content {
            includeGroup("org.bukkit")
            includeGroup("org.spigotmc")
        }
    }
    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
        content {
            includeGroup("io.papermc.paper")
            includeGroup("net.md-5")
            includeGroup("com.mojang")
        }
    }
    maven {
        url = uri("https://mvn.lumine.io/repository/maven-public/")
        content {
            includeGroup("io.lumine")
        }
    }
    maven {
        url = uri("https://repo.magmaguy.com/releases")
        content {
            includeGroup("com.magmaguy")
        }
    }
    maven {
        url = uri("https://jitpack.io")
        content {
            includeGroupByRegex("com\\.github\\..*")
        }
    }
    mavenCentral()
    // mavenLocal()
}

dependencies {
    compileOnly(libs.spigotmc.spigotapi)
    // compileOnly(libs.papermc.paperapi)
    compileOnly(libs.lumine.mythicdist)
    compileOnly(libs.magmaguy.fmm)
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name()
    }
	
    javadoc {
        options.encoding = Charsets.UTF_8.name()
    }
	
    processResources {
        filesMatching("**/plugin.yml") {
            expand( project.properties )
        }
    }

    shadowJar {

    }

    /*
    spotbugsMain {
        reports.create("html") {
            required = true
            outputLocation = file("${layout.buildDirectory.get()}/reports/spotbugs.html")
            setStylesheet("fancy-hist.xsl")
        }
    }
    */
}

/*
spotbugs {
    ignoreFailures = false
    showStackTraces = true
    showProgress = true
    effort = Effort.DEFAULT
    reportLevel = Confidence.DEFAULT
}
*/
