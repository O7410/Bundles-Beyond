import dev.kikugie.stonecutter.build.StonecutterBuildExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.the

open class LoomPluginChooser : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        val current = the<StonecutterBuildExtension>().current.parsed
        if (current < "26.1") {
            this.plugins.apply("dev.architectury.loom")
        } else {
            this.plugins.apply("dev.architectury.loom-no-remap")
            val names = listOf(
                "api", "implementation", "compileOnly", "runtimeOnly", "localRuntime"
            )
            for (it in names) {
                val loomified = "mod" + it.replaceFirstChar(Char::uppercaseChar)
                val new = this.configurations.create(loomified)
                this.configurations.named(it) {
                    extendsFrom(new)
                }
            }
            this.configurations.register("mappings") {
                this.isCanBeResolved = false
                this.isCanBeConsumed = false
            }
        }
    }

}