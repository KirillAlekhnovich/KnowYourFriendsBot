package libs

import org.gradle.api.artifacts.dsl.DependencyHandler

/**
 * A library that provides a simple API for mocking Spring beans.
 */
object SpringMockK {

    private const val springMockKVersion = "3.1.2"

    private val dependencies = listOf(
        "com.ninja-squad:springmockk:$springMockKVersion"
    )

    fun DependencyHandler.springMockKTest(configurationName: String = "testImplementation") =
        dependencies.forEach { add(configurationName, it) }
}