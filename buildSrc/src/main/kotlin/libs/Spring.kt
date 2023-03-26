package libs

import org.gradle.api.artifacts.dsl.DependencyHandler

/**
 * Spring framework.
 */
object Spring {

    private val dependencies = listOf(
        "org.springframework.boot:spring-boot-starter-data-jpa",
        "org.springframework.boot:spring-boot-starter-hateoas",
        "org.springframework.boot:spring-boot-starter-web"
    )

    private val dependenciesTest = listOf(
        "org.springframework.boot:spring-boot-starter-test"
    )

    fun DependencyHandler.spring(configurationName: String = "implementation") =
        dependencies.forEach { add(configurationName, it) }

    fun DependencyHandler.springTest(configurationName: String = "testImplementation") =
        dependenciesTest.forEach { add(configurationName, it) }
}