package com.backend.kyf

import com.backend.kyf.utils.auth.Jedis
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KyfApplication

fun main(args: Array<String>) {
    Jedis.get().flushAll()
    runApplication<KyfApplication>(*args)
}