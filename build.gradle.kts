import java.io.ByteArrayInputStream
import java.net.URI
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

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
        put("pop_matrix", if (current.parsed >= "1.21.8") "popMatrix();" else "popPose();")
        put("push_matrix", if (current.parsed >= "1.21.8") "pushMatrix();" else "pushPose();")
        // 26.1 rendering refactor: GuiGraphics → GuiGraphicsExtractor
        put("gui_graphics", if (current.parsed >= "26.1") "GuiGraphicsExtractor" else "GuiGraphics")
        put("scale_2d", if (current.parsed >= "26.1") "scale(scaleFactor, scaleFactor);" else if (current.parsed >= "1.21.8") "scale(scaleFactor);" else "scale(scaleFactor, scaleFactor, scaleFactor);")
    }
}

// MC 26.1+ is unobfuscated. Fabric's intermediary POM has version 0.0.0 causing
// Gradle metadata mismatch. Generate a local identity intermediary with correct
// POM version and v2 format.
if (stonecutter.current.parsed >= "26.1") {
    val localMaven = rootDir.resolve(".gradle/local-maven")
    val mcv = env.mcVersion.min
    val intermediaryDir = localMaven.resolve("net/fabricmc/intermediary/${mcv}")
    val pomFile = intermediaryDir.resolve("intermediary-${mcv}.pom")
    val jarFile = intermediaryDir.resolve("intermediary-${mcv}-v2.jar")
    if (!jarFile.exists()) {
        intermediaryDir.mkdirs()
        pomFile.writeText("""
            <?xml version="1.0" encoding="UTF-8"?>
            <project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
              <modelVersion>4.0.0</modelVersion>
              <groupId>net.fabricmc</groupId>
              <artifactId>intermediary</artifactId>
              <version>${mcv}</version>
            </project>
        """.trimIndent())
        val tinyContent = "tiny\t2\t0\tofficial\tintermediary\n".toByteArray()
        ZipOutputStream(jarFile.outputStream()).use { zip ->
            zip.putNextEntry(ZipEntry("mappings/mappings.tiny"))
            zip.write(tinyContent)
            zip.closeEntry()
        }
    }
    repositories {
        maven(localMaven) {
            name = "LocalIntermediary"
            content {
                includeModule("net.fabricmc", "intermediary")
            }
        }
    }
}

// Architectury Loom doesn't support NeoForm spec 6 (26.1+) which changed the
// config.json format (classpath arrays instead of version strings, no mappings).
// Provide a patched NeoForm ZIP that converts spec 6 back to spec 4 format.
if (stonecutter.current.parsed >= "26.1" && env is EnvNeo) {
    val localMaven = rootDir.resolve(".gradle/local-maven")
    val mcv = env.mcVersion.min
    val neoformVersion = "$mcv-1"
    val neoformDir = localMaven.resolve("net/neoforged/neoform/$neoformVersion")
    val patchedZip = neoformDir.resolve("neoform-$neoformVersion.zip")

    if (!patchedZip.exists()) {
        neoformDir.mkdirs()
        val neoformUrl = URI("https://maven.neoforged.net/releases/net/neoforged/neoform/$neoformVersion/neoform-$neoformVersion.zip").toURL()
        val originalBytes = neoformUrl.readBytes()

        val entries = linkedMapOf<String, ByteArray>()
        ZipInputStream(ByteArrayInputStream(originalBytes)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                entries[entry.name] = if (entry.isDirectory) ByteArray(0) else zis.readBytes()
                entry = zis.nextEntry
            }
        }

        val json = com.google.gson.JsonParser.parseString(String(entries["config.json"]!!)).asJsonObject
        // Add missing data.mappings (identity mapping) and mark as official (unobfuscated)
        json.getAsJsonObject("data").addProperty("mappings", "config/joined.tsrg")
        json.addProperty("official", true)
        // Convert functions from spec 6 (classpath array) to spec 4 (version string)
        val functions = json.getAsJsonObject("functions")
        for (key in functions.keySet()) {
            val func = functions.getAsJsonObject(key)
            if (func.has("classpath") && !func.has("version")) {
                val classpath = func.getAsJsonArray("classpath")
                if (classpath.size() > 0) {
                    func.addProperty("version", classpath[0].asString)
                }
                func.remove("classpath")
            }
            func.remove("java_version")
            // Ensure 'repo' exists (spec 4 reads it without null check)
            if (!func.has("repo")) {
                func.addProperty("repo", "https://maven.neoforged.net/releases/")
            }
        }
        entries["config.json"] = com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(json).toByteArray()
        entries["config/joined.tsrg"] = "tsrg2 left right\n".toByteArray()

        // Rename preProcessJar step to "rename" (Loom enqueues "rename" step by name)
        val joinedSteps = json.getAsJsonObject("steps").getAsJsonArray("joined")
        for (i in 0 until joinedSteps.size()) {
            val step = joinedSteps[i].asJsonObject
            if (step.get("type").asString == "preProcessJar") {
                step.addProperty("name", "rename")
            }
        }
        entries["config.json"] = com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(json).toByteArray()

        ZipOutputStream(patchedZip.outputStream()).use { zos ->
            for ((name, bytes) in entries) {
                zos.putNextEntry(ZipEntry(name))
                if (bytes.isNotEmpty()) zos.write(bytes)
                zos.closeEntry()
            }
        }
        neoformDir.resolve("neoform-$neoformVersion.pom").writeText("""
            <?xml version="1.0" encoding="UTF-8"?>
            <project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
              <modelVersion>4.0.0</modelVersion>
              <groupId>net.neoforged</groupId>
              <artifactId>neoform</artifactId>
              <version>$neoformVersion</version>
            </project>
        """.trimIndent())
    }

    repositories {
        exclusiveContent {
            forRepository {
                maven(localMaven) { name = "LocalNeoForm" }
            }
            filter {
                includeModule("net.neoforged", "neoform")
            }
        }
    }

    // Loom's AT tool bundles ASM 9.7 which doesn't support Java 25 (class version 69).
    // Use component metadata rules to upgrade ASM dependencies in the AT tool.
    dependencies {
        components {
            withModule("net.neoforged.accesstransformers:at-cli") {
                allVariants {
                    withDependencies {
                        removeAll { it.group == "org.ow2.asm" }
                        add("org.ow2.asm:asm:9.9.1")
                        add("org.ow2.asm:asm-tree:9.9.1")
                        add("org.ow2.asm:asm-commons:9.9.1")
                    }
                }
            }
            withModule("net.neoforged:accesstransformers") {
                allVariants {
                    withDependencies {
                        removeAll { it.group == "org.ow2.asm" }
                        add("org.ow2.asm:asm:9.9.1")
                        add("org.ow2.asm:asm-tree:9.9.1")
                        add("org.ow2.asm:asm-commons:9.9.1")
                    }
                }
            }
        }
    }
    // Also force ASM version via resolution strategy for configs that pick it up
    configurations.all {
        resolutionStrategy.eachDependency {
            if (requested.group == "org.ow2.asm") {
                useVersion("9.9.1")
            }
        }
    }

    // MC 26.1 has no Mojang mappings (unobfuscated). Loom's NeoForge pipeline
    // unconditionally calls mergeMojang() which throws "Failed to find official
    // mojang mappings". Fix by pre-creating a valid mappings-mojang.tiny (identity
    // mapping with extra "mojang" namespace) and cleaning stale lock files so that
    // refreshDeps stays false and mergeMojang is never invoked.
    // NOTE: After a clean Loom cache the first build will fail because the mapping
    // directory has not been created yet. Run the build a second time.
    run {
        val loomCacheDir = file("${gradle.gradleUserHomeDir}/caches/fabric-loom")
        // Delete stale lock files that force refreshDeps=true (disowned or dead PID)
        loomCacheDir.listFiles()?.filter {
            it.name.endsWith(".lock") && it.isFile
        }?.forEach {
            val content = it.readText().trim()
            val isStale = content == "disowned" || content.toLongOrNull()?.let { pid ->
                ProcessHandle.of(pid).isEmpty
            } ?: false
            if (isStale) it.delete()
        }
        // Fix mappings-mojang.tiny in the mapping directory for this version
        val versionCacheDir = loomCacheDir.resolve(mcv)
        if (versionCacheDir.isDirectory) {
            versionCacheDir.listFiles()?.filter {
                it.isDirectory && it.name.startsWith("loom.mappings.") && it.name.contains("neoforge")
            }?.forEach { mappingDir ->
                val mojangTiny = mappingDir.resolve("mappings-mojang.tiny")
                val baseTiny = mappingDir.resolve("mappings-base.tiny")
                if (baseTiny.exists() && (!mojangTiny.exists() || mojangTiny.length() == 0L)) {
                    // NeoForge remapper expects "official" as the src namespace
                    mojangTiny.writeText("tiny\t2\t0\tofficial\tintermediary\tnamed\tmojang\n")
                }
            }
        }
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
        if (stonecutter.current.parsed >= "26.1") {
            implementation("net.fabricmc.fabric-api:fabric-api:${env.fabricApiVersion.min}")
            implementation("com.terraformersmc:modmenu:${env.modmenuVersion.min}")
        } else {
            modImplementation("net.fabricmc.fabric-api:fabric-api:${env.fabricApiVersion.min}")
            modImplementation("com.terraformersmc:modmenu:${env.modmenuVersion.min}")
        }
    }
    if (env is EnvNeo) {
        "neoForge"("net.neoforged:neoforge:${env.neoforgeVersion.min}")
    }
    if (env.parchmentVersion.isNotEmpty()) {
        mappings(loom.layered {
            officialMojangMappings()
            parchment("org.parchmentmc.data:parchment-${env.mcVersion.min}:${env.parchmentVersion}@zip")
        })
    } else {
        mappings(loom.layered { })
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
            start = env.mcVersion.min
            end = env.mcVersion.max.ifEmpty { env.mcVersion.min }
        }

        if (env is EnvFabric) {
            requires("fabric-api")
            optional("modmenu")
        }
    }
}