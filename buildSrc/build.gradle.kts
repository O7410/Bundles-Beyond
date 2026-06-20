plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

gradlePlugin {
    plugins {
        register("loom-plugin-chooser") {
            id = "loom-plugin-chooser"
            implementationClass = "LoomPluginChooser"
        }
    }
}