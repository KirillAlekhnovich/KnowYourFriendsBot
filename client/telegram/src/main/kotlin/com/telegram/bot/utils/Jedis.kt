package com.telegram.bot.utils

import redis.clients.jedis.Jedis
import java.net.URI

object Jedis {

    private val jedis = Jedis(URI(System.getenv("RedisURL")))

    fun get(): Jedis = jedis

    fun Jedis.reset(userId: Long) {
        val keys = jedis.keys("user:$userId:*")
        keys.forEach {
            if (it != "user:$userId:${RedisParams.ACCESS_TOKEN}") {
                jedis.del(it)
            }
        }
    }

    private fun constructField(userId: Long, field: String): String {
        return "user:$userId:$field"
    }

    fun Jedis.getCommandsQueue(userId: Long): List<String> {
        return jedis.lrange(constructField(userId, RedisParams.COMMANDS_QUEUE.name), 0, -1)
    }

    fun Jedis.addToCommandsQueue(userId: Long, command: String) {
        jedis.rpush(constructField(userId, RedisParams.COMMANDS_QUEUE.name), command)
    }

    fun Jedis.addToCommandsQueue(userId: Long, commandsQueue: List<String>) {
        jedis.rpush(constructField(userId, RedisParams.COMMANDS_QUEUE.name), commandsQueue.joinToString(","))
    }

    fun Jedis.removeFirstCommandFromQueue(userId: Long) {
        jedis.lpop(constructField(userId, RedisParams.COMMANDS_QUEUE.name))
    }

    fun Jedis.getCurrentPage(userId: Long): Int {
        return jedis.get(constructField(userId, RedisParams.CURRENT_PAGE.name))?.toInt() ?: 1
    }

    fun Jedis.setCurrentPage(userId: Long, page: Int) {
        jedis.set(constructField(userId, RedisParams.CURRENT_PAGE.name), page.toString())
    }

    fun Jedis.incrementCurrentPage(userId: Long, incrValue: Long) {
        jedis.incrBy(constructField(userId, RedisParams.CURRENT_PAGE.name), incrValue)
    }

    fun getAccessToken(userId: Long): String? {
        if (jedis.hget(userId.toString(), RedisParams.ACCESS_TOKEN.name) == null) return null
        return "Bearer " + jedis.hget(userId.toString(), RedisParams.ACCESS_TOKEN.name)
    }
}