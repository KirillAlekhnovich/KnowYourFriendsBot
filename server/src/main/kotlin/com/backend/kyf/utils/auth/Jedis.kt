package com.backend.kyf.utils.auth

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
}