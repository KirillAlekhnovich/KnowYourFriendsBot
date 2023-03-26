package libs

import org.gradle.api.artifacts.dsl.DependencyHandler

/**
 * A library that is used to write and run tests.
 */
object JUnit {

    private const val jUnitVersion = "5.8.1"

    private val dependencies = listOf(
        "org.junit.jupiter:junit-jupiter:$jUnitVersion"
    )

    fun DependencyHandler.jUnit(configurationName: String = "implementation") =
        dependencies.forEach { add(configurationName, it) }
}

