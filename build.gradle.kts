plugins {
    id("fabric-loom")
    `maven-publish`
}

val loader_version: String by project
val mc_version_range: String by project
val yarn_mappings: String by project
val fabric_version: String by project
val modmenu_version: String by project
val mod_version: String by project
val maven_group: String by project
val archives_base_name: String by project
// by - same as this
// val loader_version: String = project.property("loader_version") as String

version = "$mod_version+${stonecutter.current.version}"
group = maven_group
base.archivesName = archives_base_name

repositories {
    maven("https://maven.terraformersmc.com") { name = "Terraformers" }
}

dependencies {
    minecraft("com.mojang:minecraft:${stonecutter.current.version}")
    mappings("net.fabricmc:yarn:$yarn_mappings:v2")
    modImplementation("net.fabricmc:fabric-loader:$loader_version")

    // Fabric API. This is technically optional, but you probably want it anyway.
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabric_version")
    modImplementation("com.terraformersmc:modmenu:$modmenu_version")
}

loom {
    decompilerOptions.named("vineflower") {
        options.put("mark-corresponding-synthetics", "1") // Adds names to lambdas - useful for mixins
    }

    runConfigs.all {
        ideConfigGenerated(true)
        vmArgs("-Dmixin.debug.export=true") // Exports transformed classes for debugging
        runDir = "../../run" // Shares the run directory between versions
    }

    splitEnvironmentSourceSets()

    mods {
        create("bundles-beyond") {
            sourceSet(sourceSets["client"])
        }
    }
}

java {
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks {

    named<ProcessResources>("processClientResources") {
        filesMatching("fabric.mod.json") {
            expand(
                "version" to project.version,
                "loader_version" to loader_version,
                "mc_version_range" to mc_version_range
            )
        }
    }

    // Builds the version into a shared folder in `build/libs/${mod version}/`
    register<Copy>("buildAndCollect") {
        group = "build"
        from(remapJar.map { it.archiveFile }, remapSourcesJar.map { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/$mod_version"))
        dependsOn("build")
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }

    repositories {
    }
}
