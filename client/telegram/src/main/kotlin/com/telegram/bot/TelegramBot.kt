package com.telegram.bot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TelegramBot

fun main(args: Array<String>) {
    runApplication<TelegramBot>(*args)
}