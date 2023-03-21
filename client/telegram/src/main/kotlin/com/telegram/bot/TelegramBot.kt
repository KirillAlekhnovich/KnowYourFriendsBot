package com.telegram.bot

import com.telegram.bot.utils.Jedis
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TelegramBot

fun main(args: Array<String>) {
    Jedis.get().flushAll()
    runApplication<TelegramBot>(*args)
}