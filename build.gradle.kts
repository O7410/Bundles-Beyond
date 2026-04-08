import me.modmuss50.mpp.MinecraftApi
import org.gradle.jvm.tasks.Jar

plugins {
    `maven-publish`
    id("loom-plugin-chooser")
    id("me.modmuss50.mod-publish-plugin") version "1.1.0"
}

repositories {
    mavenCentral()
    maven("https://maven.neoforged.net/releases/")
    maven("https://maven.terraformersmc.com/") { name = "Terraformers" }
}

val (mcVersion, loader) = stonecutter.current.project.split('-', limit = 2)
val obfuscated = stonecutter.current.parsed <= "1.21.11"

stonecutter {
    constants {
        put("fabric", loader == "fabric")
        put("neoforge", loader == "neoforge")
    }

    swaps {
        put("display_actionbar", if (current.parsed >= "26.1") "player.sendOverlayMessage($1);" else "player.displayClientMessage($1, true);")
        put("size", if (current.parsed >= "26.1") "int size = lastBundleSize;" else "int size = this.contents.size();")
        put("translate", if (current.parsed >= "1.21.8") "$1.pose().translate($2, $3);" else "$1.pose().translate($2, $3, 0);")
    }

    replacements {
        string(current.parsed >= "1.21.8") {
            replace("popPose", "popMatrix")
            replace("pushPose", "pushMatrix")
            replace("java/util/function/Function", "com/mojang/blaze3d/pipeline/RenderPipeline")
            replace("Function<ResourceLocation, RenderType> ", "RenderPipeline ")
        }

        string(current.parsed >= "1.21.11") {
            replace("ResourceLocation", "Identifier")
        }

        string(current.parsed >= "26.1") {
            replace("GuiGraphics", "GuiGraphicsExtractor")
            replace("ClickType", "ContainerInput")
        }
    }
}

sealed class Env {
    val mcRange = property("mc_version_range.$mcVersion") as String
}

class EnvFabric : Env() {
    val fabricLoader = property("fabric_loader") as String
    val fabricApi = property("fabric_api.$mcVersion") as String
    val modmenu = property("modmenu.$mcVersion") as String
}

class EnvNeo : Env() {
    val neoforgeVersion = property("neoforge.$mcVersion") as String
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
    val icon = "assets/$id/${property("mod.icon") as String}"
    val issueTracker = property("mod.issue_tracker") as String
    val license = property("mod.license") as String
    val sourceUrl = property("mod.source_url") as String
    val homepage = property("mod.homepage") as String
    val mixinsFile = "$id.mixins.json"
}

val mod = ModProperties()

version = "${mod.version}+$mcVersion+$loader"
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

    if (env is EnvFabric) {
        modImplementation("net.fabricmc:fabric-loader:${env.fabricLoader}")
        modImplementation("net.fabricmc.fabric-api:fabric-api:${env.fabricApi}")
        modImplementation("com.terraformersmc:modmenu:${env.modmenu}")
    }
    if (env is EnvNeo) {
        "neoForge"("net.neoforged:neoforge:${env.neoforgeVersion}")
    }
    if (obfuscated) {
        mappings(loom.layered {
            officialMojangMappings()
            parchment("org.parchmentmc.data:parchment-${mcVersion}:${property("parchment.$mcVersion")}@zip")
        })
    }

    vineflowerDecompilerClasspath("org.vineflower:vineflower:1.11.2")
}

java {
    withSourcesJar()
    val javaVersion = if (stonecutter.current.parsed >= "26.1") JavaVersion.VERSION_25 else JavaVersion.VERSION_21
    targetCompatibility = javaVersion
    sourceCompatibility = javaVersion
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
            is EnvFabric -> fabricMcRange()
            is EnvNeo -> env.mcRange
        },
        "issue_tracker" to mod.issueTracker,
        "loader" to when (env) {
            is EnvFabric -> env.fabricLoader
            is EnvNeo -> env.neoforgeVersion
        },
        "loader_name" to loader,
        "license" to mod.license,
        "mixins_file" to mod.mixinsFile
    )
    map.forEach(inputs::property)
    exclude(when (env) {
        is EnvFabric -> "META-INF"
        is EnvNeo -> "fabric.mod.json"
    })
    filesMatching("fabric.mod.json") { expand(map) }
    filesMatching("META-INF/neoforge.mods.toml") { expand(map) }
}

fun fabricMcRange(): String {
    val parts = env.mcRange.split(",")
    if (parts.size == 1) {
        assert(env.mcRange.startsWith("["))
        assert(env.mcRange.endsWith("]"))
        return env.mcRange.substring(1, env.mcRange.length - 1)
    }
    val (first, second) = parts.also { assert(it.size == 2) }
    assert(first.length > 1)
    val lowerBound = when (first.first()) {
        '[' -> ">="
        '(' -> ">"
        else -> throw IllegalArgumentException("Invalid mcRange: ${env.mcRange}")
    } + first.drop(1)
    val upperBound = when (second.last()) {
        ']' -> {
            assert(second.length > 1)
            "<=" + second.dropLast(1)
        }
        ')' -> {
            if (second.length == 1) null
            else "<" + second.substring(1)
        }
        else -> throw IllegalArgumentException("Invalid mcRange: ${env.mcRange}")
    }
    return listOfNotNull(lowerBound, upperBound)
        .joinToString(" ")
        .ifEmpty { "*" }
}

publishMods {
    changelog = System.getenv("CHANGELOG")
    type = STABLE

    val jar = tasks.named(
        if (obfuscated) "remapJar" else "jar"
    ).get() as Jar
    file = jar.archiveFile

    val sourcesJar = tasks.named(
        if (obfuscated) "remapSourcesJar" else "sourcesJar"
    ).get() as Jar
    additionalFiles.from(sourcesJar.archiveFile)

    when (env) {
        is EnvFabric -> modLoaders.addAll("fabric", "quilt")
        is EnvNeo -> modLoaders.add("neoforge")
    }

    modrinth {
        accessToken = providers.environmentVariable("MODRINTH_API_KEY")
        projectId = "VhUy58Cq"
        displayName = "Bundles Beyond ${version.get()}"
        minecraftVersions.addAll(getMinecraftVersionsForModrinth())

        if (env is EnvFabric) {
            requires("fabric-api")
            optional("modmenu")
        }
    }
}

fun getMinecraftVersionsForModrinth(): List<String> {
    val parts = env.mcRange.split(",")
    if (parts.size == 1) {
        assert(env.mcRange.startsWith("["))
        assert(env.mcRange.endsWith("]"))
        return listOf(env.mcRange.substring(1, env.mcRange.length - 1))
    }
    val startId = parts[0].drop(1)
    val excludeStart = parts[0].first() == '('
    val excludeEnd = parts[1].last() == ')'
    val endId = parts[1].dropLast(1).ifEmpty { "latest".also { assert(excludeEnd) } }

    val versions = MinecraftApi().getVersions()
        .filter { it.type == "release" }
        .map { it.id }
        .reversed()

    val startIndex = versions.indexOf(startId).let {
        if (it == -1) throw IllegalArgumentException("Invalid start version $startId")
        it + if (excludeStart) 1 else 0
    }

    val endIndex = if (endId == "latest")
        versions.size - 1
    else
        versions.indexOf(endId).let {
            if (it == -1) throw IllegalArgumentException("Invalid end version $endId")
            it + if (excludeEnd) 0 else 1
        }

    if (startIndex > endIndex) throw IllegalArgumentException("Start version $startId must be before end version $endId")

    return versions.subList(startIndex, endIndex)
}