pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.minecraftforge.net")
        maven("https://maven.architectury.dev")
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9"
}

val versions = listOf("1.21.3", "1.21.4", "1.21.8", "1.21.10", "1.21.11", "26.1")

stonecutter {
    create(rootProject) {
        for (version in versions) {
            version("$version-fabric", version)
            version("$version-neoforge", version)
        }
        vcsVersion = "26.1-fabric"
    }
}