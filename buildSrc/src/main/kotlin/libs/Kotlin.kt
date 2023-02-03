package libs

import org.gradle.api.artifacts.dsl.DependencyHandler

object Kotlin {

    private val dependencies = listOf(
        "org.jetbrains.kotlin:kotlin-reflect"
    )

    fun DependencyHandler.kotlin(configurationName: String = "implementation") =
        dependencies.forEach { add(configurationName, it) }
}