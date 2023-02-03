package libs

import org.gradle.api.artifacts.dsl.DependencyHandler

object Fuel {

    private const val fuelVersion = "2.3.1"

    private val dependencies = listOf(
        "com.github.kittinunf.fuel:fuel:$fuelVersion"
    )

    fun DependencyHandler.fuel(configurationName: String = "implementation") =
        dependencies.forEach { add(configurationName, it) }
}