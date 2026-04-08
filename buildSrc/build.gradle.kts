plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
}

dependencies {
    implementation("dev.kikugie:stonecutter:0.9")
}

gradlePlugin {
    plugins {
        register("loom-plugin-chooser") {
            id = "loom-plugin-chooser"
            implementationClass = "LoomPluginChooser"
        }
    }
}