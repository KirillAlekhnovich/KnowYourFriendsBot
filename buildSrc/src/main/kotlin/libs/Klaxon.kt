package libs

import org.gradle.api.artifacts.dsl.DependencyHandler

/**
 * A library that provides a simple API for parsing JSON.
 */
object Klaxon {

    private const val klaxonVersion = "5.5"

    private val dependencies = listOf(
        "com.beust:klaxon:$klaxonVersion"
    )

    fun DependencyHandler.klaxon(configurationName: String = "implementation") =
        dependencies.forEach { add(configurationName, it) }
}