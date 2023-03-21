package com.backend.kyf.utils.auth

import redis.clients.jedis.Jedis
import java.net.URI

object Jedis {

    private val jedis = Jedis(URI(System.getenv("RedisURL")))

    fun get(): Jedis = jedis
}