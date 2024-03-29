package libs

import org.gradle.api.artifacts.dsl.DependencyHandler

/**
 * A library that provides a mocking API.
 */
object MockK {
    private const val mockKVersion = "1.13.4"

    private val dependencies = listOf(
        "io.mockk:mockk:$mockKVersion"
    )

    fun DependencyHandler.mockKTest(configurationName: String = "testImplementation") =
        dependencies.forEach { add(configurationName, it) }
}