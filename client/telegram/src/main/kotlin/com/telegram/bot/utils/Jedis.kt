package com.telegram.bot.utils

import redis.clients.jedis.Jedis
import java.net.URI

/**
 * Object for setting and retrieving values from Redis database.
 */
object Jedis {

    private val jedis = Jedis(URI(System.getenv("RedisURL")))

    /**
     * Sets value to Redis database.
     */
    fun setValue(userId: Long, key: String, value: String) {
        jedis.hset(userId.toString(), key, value)
    }

    /**
     * Gets value from Redis database.
     */
    fun getValue(userId: Long, key: String): String? {
        return jedis.hget(userId.toString(), key)
    }

    /**
     * Deletes value from Redis database.
     */
    fun deleteValue(userId: Long, key: String) {
        jedis.hdel(userId.toString(), key)
    }

    /**
     * Checks if value exists in Redis database.
     */
    fun exists(userId: Long, key: String): Boolean {
        return jedis.hexists(userId.toString(), key)
    }

    /**
     * Resets user's data in Redis database while keeping his access token.
     */
    fun reset(userId: Long) {
        val keys = jedis.keys("user:$userId:*")
        keys.forEach {
            if (it != "user:$userId:${RedisParams.ACCESS_TOKEN}" && it != "user:$userId:${RedisParams.COMMANDS_QUEUE}") {
                jedis.del(it)
            }
        }
    }

    /**
     * Constructs field name for Redis database.
     */
    private fun constructField(userId: Long, field: String): String {
        return "user:$userId:$field"
    }

    /**
     * Gets user's queue of next commands from Redis database.
     */
    fun getCommandsQueue(userId: Long): List<String> {
        return jedis.lrange(constructField(userId, RedisParams.COMMANDS_QUEUE.name), 0, -1)
    }

    /**
     * Adds command to user's queue of next commands in Redis database.
     */
    fun addToCommandsQueue(userId: Long, command: String) {
        jedis.rpush(constructField(userId, RedisParams.COMMANDS_QUEUE.name), command)
    }

    /**
     * Adds list of commands to user's queue of next commands in Redis database.
     */
    fun addToCommandsQueue(userId: Long, commandsQueue: List<String>) {
        jedis.rpush(constructField(userId, RedisParams.COMMANDS_QUEUE.name), commandsQueue.joinToString(","))
    }

    /**
     * Removes first command from user's queue of next commands in Redis database.
     */
    fun removeFirstCommandFromQueue(userId: Long) {
        jedis.lpop(constructField(userId, RedisParams.COMMANDS_QUEUE.name))
    }

    /**
     * Gets friend list's current page from Redis database.
     */
    fun getCurrentPage(userId: Long): Int {
        return jedis.get(constructField(userId, RedisParams.CURRENT_PAGE.name))?.toInt() ?: 1
    }

    /**
     * Changes friend list's current page in Redis database.
     */
    fun incrementCurrentPage(userId: Long, incrValue: Long) {
        jedis.incrBy(constructField(userId, RedisParams.CURRENT_PAGE.name), incrValue)
    }

    /**
     * Gets user's access token from Redis database.
     */
    fun getAccessToken(userId: Long): String? {
        if (jedis.hget(userId.toString(), RedisParams.ACCESS_TOKEN.name) == null) return null
        return "Bearer " + jedis.hget(userId.toString(), RedisParams.ACCESS_TOKEN.name)
    }
}