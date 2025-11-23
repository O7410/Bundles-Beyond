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

fun listProperty(key: String): ArrayList<String> {
    val str = property(key)!! as String
    return ArrayList(str.split(" "))
}

class VersionRange(val min: String, val max: String) {
    fun asNeoforge(): String {
        if (min == max) return "[$min]"
        val opener = if (min.isEmpty()) "(" else "["
        val closer = if (max.isEmpty()) ")" else "]"
        return "$opener$min,$max$closer"
    }

    fun asFabric(): String {
        var out = ""
        if (min.isNotEmpty() && min == max) {
            return min
        }
        if (min.isNotEmpty()) {
            out += ">=$min"
        }
        if (max.isNotEmpty()) {
            if (out.isNotEmpty()) {
                out += " "
            }
            out += "<=$max"
        }
        return out
    }
}

/**
 * Creates a VersionRange from a listProperty
 */
fun versionProperty(key: String): VersionRange {
    val list = listProperty(key)
    for (i in 0 until list.size) {
        if (list[i] == "UNSET") {
            list[i] = ""
        }
    }
    return when (list.size) {
        0 -> VersionRange("", "")
        1 -> VersionRange(list[0], "")
        else -> VersionRange(list[0], list[1])
    }
}

/**
 * Stores core dependency and environment information.
 */
sealed class Env {
    val archivesBaseName = property("archives_base_name").toString()

    val mcVersion = versionProperty("deps.core.mc")

    val loader = property("loom.platform") as String

    val yarnMappings = property("deps.core.yarn") as String
    val yarnNeoforgePatch = property("deps.core.yarn.neoforge_patch") as String
}

class EnvFabric : Env() {
    val fabricLoaderVersion = versionProperty("deps.core.fabric")
    val fabricApiVersion = versionProperty("deps.api.fabric_api")
    val modmenuVersion = if (hasProperty("deps.api.modmenu")) versionProperty("deps.api.modmenu") else null
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

stonecutter.constants {
    put("fabric", env is EnvFabric)
    put("neoforge", env is EnvNeo)
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
        mappings(loom.officialMojangMappings())
//        mappings("net.fabricmc:yarn:${env.yarnMappings}:v2")

        modImplementation("net.fabricmc.fabric-api:fabric-api:${env.fabricApiVersion.min}")
        env.modmenuVersion?.let { modImplementation("com.terraformersmc:modmenu:${it.min}") }
    }
    if (env is EnvNeo) {
        "neoForge"("net.neoforged:neoforge:${env.neoforgeVersion.min}")
        mappings(loom.officialMojangMappings())
//        mappings(loom.layered {
//            mappings("net.fabricmc:yarn:${env.yarnMappings}:v2")
//            mappings("dev.architectury:yarn-mappings-patch-neoforge:${env.yarnNeoforgePatch}")
//        })
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
        hmmm I haven't checked my mod in a while- WAIT WHAT<br>
        Thank you so much for 100K downloads ♥️<br>
        This is a small release that adds a feature that was suggested on the github [here](https://github.com/O7410/Bundles-Beyond/issues/3#issuecomment-3528217363)<br>
        The progress bar now shows a fraction of the capacity out of 64<br>
        ![](https://cdn.modrinth.com/data/cached_images/3f66e5fb7f33104b92789579b35c695103b9b938.png)
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