plugins {
    id("dev.kikugie.stonecutter")
    id("dev.architectury.loom") version "1.17-SNAPSHOT" apply false
    id("dev.architectury.loom-no-remap") version "1.17-SNAPSHOT" apply false
}
stonecutter active "26.1-fabric"

subprojects {
    extra["loom.platform"] = project.name.split('-').last()
}

stonecutter tasks {
    // Make newer versions be published last
    order("publishModrinth")
}