package libs

import org.gradle.api.artifacts.dsl.DependencyHandler

object SpringSecurity {

    private val dependencies = listOf(
        "org.springframework.boot:spring-boot-starter-security"
    )

    fun DependencyHandler.springSecurity(configurationName: String = "implementation") =
        dependencies.forEach { add(configurationName, it) }
}