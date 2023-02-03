package com.telegram.bot

import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class TelegramBot

fun main(args: Array<String>) {
    org.springframework.boot.runApplication<TelegramBot>(*args)
}