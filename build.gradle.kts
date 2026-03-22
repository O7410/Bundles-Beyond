plugins {
    `maven-publish`
    id("dev.architectury.loom")
    id("me.modmuss50.mod-publish-plugin") version "1.1.0"
}

repositories {
    mavenCentral()
    maven("https://maven.neoforged.net/releases/")
    maven("https://maven.terraformersmc.com/") { name = "Terraformers" }
}

val (mcVersion, loader) = stonecutter.current.project.split('-', limit = 2)

stonecutter {
    constants {
        put("fabric", loader == "fabric")
        put("neoforge", loader == "neoforge")
    }

    swaps {
        put("resource_location", if (current.parsed <= "1.21.10") "ResourceLocation" else "Identifier")
        put("pop_matrix", if (current.parsed >= "1.21.8") "popMatrix();" else "popPose();")
        put("push_matrix", if (current.parsed >= "1.21.8") "pushMatrix();" else "pushPose();")
    }

    properties.tags(mcVersion, loader)
}

sealed class Env {
    val maxMcVersion = findProperty("max_mc_version") as String?
}

class EnvFabric : Env() {
    val fabricLoader = property("fabric_loader") as String
    val fabricApi = property("fabric_api") as String
    val modmenu = property("modmenu") as String
}

class EnvNeo : Env() {
    val neoforgeVersion = property("neoforge") as String
}

val env = when (loader) {
    "fabric" -> EnvFabric()
    "neoforge" -> EnvNeo()
    else -> throw GradleException("Unsupported loader: $loader")
}

class ModProperties {
    val id = property("mod.id") as String
    val displayName = property("mod.display_name") as String
    val version = property("mod.version") as String
    val description = property("mod.description") as String
    val authors = property("mod.authors") as String
    val icon = "assets/$id/${property("mod.icon")!!}"
    val issueTracker = property("mod.issue_tracker") as String
    val license = property("mod.license") as String
    val sourceUrl = property("mod.source_url") as String
    val homepage = "mod.homepage".let { if (hasProperty(it)) property(it) as String else sourceUrl }
    val mixinsFile = "${id}.mixins.json"
}

val mod = ModProperties()

version = "${mod.version}+${mcVersion}+${loader}"
group = property("maven_group").toString()

loom {
    decompilers {
        get("vineflower").apply { // Adds names to lambdas - useful for mixins
            options.put("mark-corresponding-synthetics", "1")
        }
    }

    runConfigs.all {
        ideConfigGenerated(stonecutter.current.isActive)
        vmArgs("-Dmixin.debug.export=true")
        runDir = "../../run"
    }
}


base.archivesName = property("archives_base_name") as String

dependencies {
    minecraft("com.mojang:minecraft:${mcVersion}")

    fun modImplementation(dependencyNotation: String) {
        if (stonecutter.current.parsed <= "1.21.11") {
            this.modImplementation(dependencyNotation)
        } else {
            this.implementation(dependencyNotation)
        }
    }
    if (env is EnvFabric) {
        modImplementation("net.fabricmc:fabric-loader:${env.fabricLoader}")
        modImplementation("net.fabricmc.fabric-api:fabric-api:${env.fabricApi}")
        modImplementation("com.terraformersmc:modmenu:${env.modmenu}")
    }
    if (env is EnvNeo) {
        "neoForge"("net.neoforged:neoforge:${env.neoforgeVersion}")
    }
    if (stonecutter.current.parsed <= "1.21.11") {
        mappings(loom.layered {
            officialMojangMappings()
            parchment("org.parchmentmc.data:parchment-${mcVersion}:${property("parchment")}@zip")
        })
    }

    vineflowerDecompilerClasspath("org.vineflower:vineflower:1.11.2")
}

java {
    withSourcesJar()
    targetCompatibility = JavaVersion.VERSION_21
    sourceCompatibility = JavaVersion.VERSION_21
}

tasks.processResources {
    val map = mapOf(
        "mod_id" to mod.id,
        "name" to mod.displayName,
        "version" to mod.version,
        "description" to mod.description,
        "authors" to mod.authors,
        "source_url" to mod.sourceUrl,
        "website" to mod.homepage,
        "icon" to mod.icon,
        "mc_ver" to mcVersion,
        "mc_range" to when (env) {
            is EnvFabric -> when (env.maxMcVersion) {
                null -> mcVersion
                "" -> ">=$mcVersion"
                else -> ">=$mcVersion <=${env.maxMcVersion}"
            }
            is EnvNeo -> when (env.maxMcVersion) {
                null -> "[$mcVersion]"
                "" -> "$[$mcVersion,)"
                else -> "[$mcVersion,${env.maxMcVersion}]"
            }
        },
        "issue_tracker" to mod.issueTracker,
        "loader" to when (env) {
            is EnvFabric -> env.fabricLoader
            is EnvNeo -> env.neoforgeVersion
        },
        "api" to when (env) {
            is EnvFabric -> env.fabricApi
            is EnvNeo -> env.neoforgeVersion
        },
        "loader_name" to loader,
        "license" to mod.license,
        "mixins_file" to mod.mixinsFile
    )
    map.forEach { (key, value) ->
        inputs.property(key, value)
    }
    if (env !is EnvFabric) {
        exclude("fabric.mod.json")
    }
    if (env !is EnvNeo) {
        exclude("META-INF")
    }
    filesMatching("fabric.mod.json") { expand(map) }
    filesMatching("META-INF/neoforge.mods.toml") { expand(map) }
}

publishMods {
    changelog = property("changelog") as String
    type = STABLE

    file = tasks.remapJar.get().archiveFile
    additionalFiles.from(tasks.remapSourcesJar.get().archiveFile)
    when (env) {
        is EnvFabric -> modLoaders.addAll("fabric", "quilt")
        is EnvNeo -> modLoaders.add("neoforge")
    }

    modrinth {
        accessToken = providers.environmentVariable("MODRINTH_API_KEY")
        projectId = "VhUy58Cq"
        displayName = "Bundles Beyond ${version.get()}"
        minecraftVersionRange {
            start = mcVersion
            end = env.maxMcVersion ?: mcVersion
        }

        if (env is EnvFabric) {
            requires("fabric-api")
            optional("modmenu")
        }
    }
}