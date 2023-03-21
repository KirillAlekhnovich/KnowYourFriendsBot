package com.telegram.bot.utils

import redis.clients.jedis.Jedis
import java.net.URI

object Jedis {

    private val jedis = Jedis(URI(System.getenv("RedisURL")))

    fun setValue(userId: Long, key: String, value: String) {
        jedis.hset(userId.toString(), key, value)
    }

    fun getValue(userId: Long, key: String): String? {
        return jedis.hget(userId.toString(), key)
    }

    fun deleteValue(userId: Long, key: String) {
        jedis.hdel(userId.toString(), key)
    }

    fun exists(userId: Long, key: String): Boolean {
        return jedis.hexists(userId.toString(), key)
    }

    fun reset(userId: Long) {
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

    fun getCommandsQueue(userId: Long): List<String> {
        return jedis.lrange(constructField(userId, RedisParams.COMMANDS_QUEUE.name), 0, -1)
    }

    fun addToCommandsQueue(userId: Long, command: String) {
        jedis.rpush(constructField(userId, RedisParams.COMMANDS_QUEUE.name), command)
    }

    fun addToCommandsQueue(userId: Long, commandsQueue: List<String>) {
        jedis.rpush(constructField(userId, RedisParams.COMMANDS_QUEUE.name), commandsQueue.joinToString(","))
    }

    fun removeFirstCommandFromQueue(userId: Long) {
        jedis.lpop(constructField(userId, RedisParams.COMMANDS_QUEUE.name))
    }

    fun getCurrentPage(userId: Long): Int {
        return jedis.get(constructField(userId, RedisParams.CURRENT_PAGE.name))?.toInt() ?: 1
    }

    fun setCurrentPage(userId: Long, page: Int) {
        jedis.set(constructField(userId, RedisParams.CURRENT_PAGE.name), page.toString())
    }

    fun incrementCurrentPage(userId: Long, incrValue: Long) {
        jedis.incrBy(constructField(userId, RedisParams.CURRENT_PAGE.name), incrValue)
    }

    fun getAccessToken(userId: Long): String? {
        if (jedis.hget(userId.toString(), RedisParams.ACCESS_TOKEN.name) == null) return null
        return "Bearer " + jedis.hget(userId.toString(), RedisParams.ACCESS_TOKEN.name)
    }
}