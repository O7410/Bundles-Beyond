pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.minecraftforge.net")
        maven("https://maven.architectury.dev")
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.7.10"
}

val versions = listOf("1.21.3", "1.21.4", "1.21.8", "1.21.10")
val loaders = listOf("fabric", "neoforge")

stonecutter {
    create(rootProject) {
        loaders.forEach { loader -> versions.forEach { version -> version("$version-$loader", version) } }
        vcsVersion = "1.21.10-fabric"
    }
}
