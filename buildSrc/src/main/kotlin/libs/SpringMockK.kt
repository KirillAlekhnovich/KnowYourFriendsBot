package libs

import org.gradle.api.artifacts.dsl.DependencyHandler

object SpringMockK {

    private const val springMockKVersion = "3.1.2"

    private val dependencies = listOf(
        "com.ninja-squad:springmockk:$springMockKVersion"
    )

    fun DependencyHandler.springMockKTest(configurationName: String = "testImplementation") =
        dependencies.forEach { add(configurationName, it) }
}