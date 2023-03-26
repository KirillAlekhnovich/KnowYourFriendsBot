package libs

import org.gradle.api.artifacts.dsl.DependencyHandler

/**
 * A library that provides a reflection API.
 */
object Kotlin {

    private val dependencies = listOf(
        "org.jetbrains.kotlin:kotlin-reflect"
    )

    fun DependencyHandler.kotlin(configurationName: String = "implementation") =
        dependencies.forEach { add(configurationName, it) }
}