import me.modmuss50.mpp.MinecraftApi
import org.gradle.jvm.tasks.Jar

plugins {
    `maven-publish`
    id("loom-plugin-chooser")
    id("me.modmuss50.mod-publish-plugin") version "2.0.1"
}

repositories {
    mavenCentral()
    maven("https://maven.neoforged.net/releases/")
    maven("https://maven.terraformersmc.com/") { name = "Terraformers" }
}

val (mcVersionName, loaderName) = stonecutter.current.project.split('-', limit = 2)
val mcVersion = findProperty("mc_version.$mcVersionName") ?: mcVersionName
val obfuscated = stonecutter.current.parsed <= "1.21.11"

stonecutter {
    constants {
        put("fabric", loaderName == "fabric")
        put("neoforge", loaderName == "neoforge")
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

// versioned property
fun verProp(key: String) = property("$key.$mcVersionName")

enum class Loader {
    FABRIC,
    NEOFORGE
}

val loader = when (loaderName) {
    "fabric" -> Loader.FABRIC
    "neoforge" -> Loader.NEOFORGE
    else -> throw GradleException("This shouldn't happen!")
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

version = "${mod.version}+$mcVersionName+$loaderName"
group = property("maven_group").toString()

loom {
    decompilers {
        get("vineflower").apply { // Adds names to lambdas - useful for mixins
            options.put("mark-corresponding-synthetics", "1")
        }
    }

    runConfigs.all {
        generateRunConfig = stonecutter.current.isActive
        jvmArguments.add("-Dmixin.debug.export=true")
        runDirectory = file("../../run")
    }
}

base.archivesName = property("archives_base_name") as String

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")

    when (loader) {
        Loader.FABRIC -> {
            modImplementation("net.fabricmc:fabric-loader:${property("fabric_loader")}")
            modImplementation("net.fabricmc.fabric-api:fabric-api:${verProp("fabric_api")}")
            modImplementation("com.terraformersmc:modmenu:${verProp("modmenu")}")
        }
        Loader.NEOFORGE -> {
            "neoForge"("net.neoforged:neoforge:${verProp("neoforge")}")
        }
    }
    if (obfuscated) {
        mappings(loom.layered {
            officialMojangMappings()
            parchment("org.parchmentmc.data:parchment-$mcVersion:${verProp("parchment")}@zip")
        })
    }

    vineflowerDecompilerClasspath("org.vineflower:vineflower:1.12.0")
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
        "mc_range" to when (loader) {
            Loader.FABRIC -> fabricMcRange(verProp("mc_version_range") as String)
            Loader.NEOFORGE -> verProp("mc_version_range")
        },
        "issue_tracker" to mod.issueTracker,
        "loader" to when (loader) {
            Loader.FABRIC -> project.property("fabric_loader")
            Loader.NEOFORGE -> verProp("neoforge")
        },
        "loader_name" to loaderName,
        "license" to mod.license,
        "mixins_file" to mod.mixinsFile
    )
    map.forEach(inputs::property)
    exclude(when (loader) {
        Loader.FABRIC -> "META-INF"
        Loader.NEOFORGE -> "fabric.mod.json"
    })
    filesMatching("fabric.mod.json") { expand(map) }
    filesMatching("META-INF/neoforge.mods.toml") { expand(map) }
}

fun fabricMcRange(mcRange: String): String {
    val parts = mcRange.split(",")
    if (parts.size == 1) {
        assert(mcRange.startsWith("["))
        assert(mcRange.endsWith("]"))
        return mcRange.substring(1, mcRange.length - 1)
    }
    val (first, second) = parts.also { assert(it.size == 2) }
    assert(first.length > 1)
    val lowerBound = when (first.first()) {
        '[' -> ">="
        '(' -> ">"
        else -> throw IllegalArgumentException("Invalid mcRange: $mcRange")
    } + first.drop(1)
    val upperBound = when (second.last()) {
        ']' -> {
            assert(second.length > 1)
            "<=" + second.dropLast(1)
        }
        ')' -> {
            if (second.length == 1) null
            else "<" + second.dropLast(1)
        }
        else -> throw IllegalArgumentException("Invalid mcRange: $mcRange")
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

    when (loader) {
        Loader.FABRIC -> modLoaders.addAll("fabric", "quilt")
        Loader.NEOFORGE -> modLoaders.add("neoforge")
    }

    modrinth {
        accessToken = providers.environmentVariable("MODRINTH_API_KEY")
        projectId = "VhUy58Cq"
        displayName = "Bundles Beyond ${version.get()}"
        minecraftVersions.addAll(getMinecraftVersionsForModrinth())

        if (loader == Loader.FABRIC) {
            requires("fabric-api")
            optional("modmenu")
        }
    }
}

fun getMinecraftVersionsForModrinth(): List<String> {
    val mcRange = verProp("mc_version_range") as String
    val parts = mcRange.split(",")
    if (parts.size == 1) {
        assert(mcRange.startsWith("["))
        assert(mcRange.endsWith("]"))
        return listOf(mcRange.substring(1, mcRange.length - 1))
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
        versions.size
    else
        versions.indexOf(endId).let {
            if (it == -1) throw IllegalArgumentException("Invalid end version $endId")
            it + if (excludeEnd) 0 else 1
        }

    if (startIndex > endIndex) throw IllegalArgumentException("Start version $startId must be before end version $endId")

    return versions.subList(startIndex, endIndex)
}