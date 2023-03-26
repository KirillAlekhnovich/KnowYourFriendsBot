package libs

import org.gradle.api.artifacts.dsl.DependencyHandler

/**
 * A client library for Telegram.
 */
object Telegram {

    private const val telegramVersion = "5.4.0.1"

    private val dependencies = listOf(
        "org.telegram:telegrambots-spring-boot-starter:$telegramVersion"
    )

    fun DependencyHandler.telegram(configurationName: String = "implementation") =
        dependencies.forEach { add(configurationName, it) }
}