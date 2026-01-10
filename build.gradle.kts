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

class VersionRange(val min: String, val max: String) {
    fun asNeoforge(): String {
        if (min == max) return "[$min]"
        val closer = if (max.isEmpty()) ")" else "]"
        return "[$min,$max$closer"
    }

    fun asFabric(): String {
        if (min == max) return min
        return ">=$min" + if (max.isNotEmpty()) " <=$max" else ""
    }
}

/**
 * Creates a VersionRange from a property
 */
fun versionProperty(key: String): VersionRange {
    val str = property(key) as String
    val list = str.split(" ")
    return when (list.size) {
        1 -> VersionRange(list[0], "")
        2 -> VersionRange(list[0], list[1])
        else -> throw GradleException("Invalid version range: $str")
    }
}

/**
 * Stores core dependency and environment information.
 */
sealed class Env {
    val archivesBaseName = property("archives_base_name").toString()
    val mcVersion = versionProperty("deps.core.mc")
    val loader = property("loom.platform") as String
    val parchmentVersion = property("deps.core.parchment") as String
}

class EnvFabric : Env() {
    val fabricLoaderVersion = versionProperty("deps.core.fabric")
    val fabricApiVersion = versionProperty("deps.api.fabric_api")
    val modmenuVersion = versionProperty("deps.api.modmenu")
}

class EnvNeo : Env() {
    val neoforgeVersion = versionProperty("deps.core.neoforge")
}

val env = property("loom.platform").let { loader ->
    when (loader) {
        "fabric" -> EnvFabric()
        "neoforge" -> EnvNeo()
        else -> throw GradleException("Unsupported loader: $loader")
    }
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

version = "${mod.version}+${env.mcVersion.min}+${env.loader}"
group = property("maven_group").toString()

stonecutter {
    constants {
        put("fabric", env is EnvFabric)
        put("neoforge", env is EnvNeo)
    }

    swaps {
        put("resource_location", if (current.parsed <= "1.21.10") "ResourceLocation" else "Identifier")
    }
}

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

base.archivesName = env.archivesBaseName

dependencies {
    minecraft("com.mojang:minecraft:${env.mcVersion.min}")

    if (env is EnvFabric) {
        modImplementation("net.fabricmc:fabric-loader:${env.fabricLoaderVersion.min}")
        modImplementation("net.fabricmc.fabric-api:fabric-api:${env.fabricApiVersion.min}")
        modImplementation("com.terraformersmc:modmenu:${env.modmenuVersion.min}")
    }
    if (env is EnvNeo) {
        "neoForge"("net.neoforged:neoforge:${env.neoforgeVersion.min}")
    }
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${env.mcVersion.min}:${env.parchmentVersion}@zip")
    })

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
        "mc_ver" to env.mcVersion.min,
        "mc_range" to when (env) {
            is EnvFabric -> env.mcVersion.asFabric()
            is EnvNeo -> env.mcVersion.asNeoforge()
        },
        "issue_tracker" to mod.issueTracker,
        "loader_range" to when (env) {
            is EnvFabric -> env.fabricLoaderVersion.asFabric()
            is EnvNeo -> env.neoforgeVersion.asNeoforge()
        },
        when (env) {
            is EnvFabric -> "fabric_api_range" to env.fabricApiVersion.asFabric()
            is EnvNeo -> "neoforge_range" to env.neoforgeVersion.asNeoforge()
        },
        "loader_name" to env.loader,
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
    changelog = """
    """.trimIndent()
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
            start = env.mcVersion.min
            end = env.mcVersion.max.ifEmpty { env.mcVersion.min }
        }

        if (env is EnvFabric) {
            requires("fabric-api")
            optional("modmenu")
        }
    }
}