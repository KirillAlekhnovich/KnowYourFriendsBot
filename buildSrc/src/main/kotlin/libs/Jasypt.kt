package libs

import org.gradle.api.artifacts.dsl.DependencyHandler

/**
 * A library that provides a simple encryption and decryption API.
 */
object Jasypt {

    private const val jasyptVersion = "1.9.3"

    private val dependencies = listOf(
        "org.jasypt:jasypt:$jasyptVersion"
    )

    fun DependencyHandler.jasypt(configurationName: String = "implementation") =
        dependencies.forEach { add(configurationName, it) }
}