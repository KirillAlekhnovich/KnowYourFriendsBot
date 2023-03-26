package libs

import org.gradle.api.artifacts.dsl.DependencyHandler

/**
 * A lightweight in-memory database used for testing.
 */
object H2 {

    private const val h2Version = "2.1.214"

    private val dependenciesTest = listOf(
        "com.h2database:h2:$h2Version"
    )

    private val dependenciesTestRuntime = listOf(
        "com.h2database:h2"
    )

    fun DependencyHandler.h2Test(configurationName: String = "testImplementation") =
        dependenciesTest.forEach { add(configurationName, it) }

    fun DependencyHandler.h2TestRuntime(configurationName: String = "testRuntimeOnly") =
        dependenciesTestRuntime.forEach { add(configurationName, it) }
}