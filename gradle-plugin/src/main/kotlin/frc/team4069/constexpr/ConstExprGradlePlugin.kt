package frc.team4069.constexpr

import org.gradle.api.Plugin
import org.gradle.api.Project

class ConstExprGradlePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.create("constexpr", ConstExprExtension::class.java)
    }
}

open class ConstExprExtension {
    var enabled: Boolean = true
}