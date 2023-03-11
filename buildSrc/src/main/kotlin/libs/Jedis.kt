package libs

import org.gradle.api.artifacts.dsl.DependencyHandler

object Jedis {

    private const val jedisVersion = "4.3.1"

    private val dependencies = listOf(
        "redis.clients:jedis:$jedisVersion"
    )

    fun DependencyHandler.jedis(configurationName: String = "implementation") =
        dependencies.forEach { add(configurationName, it) }
}