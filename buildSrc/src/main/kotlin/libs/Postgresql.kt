package libs

import org.gradle.api.artifacts.dsl.DependencyHandler

/**
 * PostgreSQL database driver.
 */
object Postgresql {

    private val dependencies = listOf(
        "org.postgresql:postgresql"
    )

    fun DependencyHandler.postgresql(configurationName: String = "runtimeOnly") =
        dependencies.forEach { add(configurationName, it) }
}