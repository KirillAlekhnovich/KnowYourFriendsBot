package com.telegram.bot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication(exclude = [DataSourceAutoConfiguration::class])
class TelegramBot

fun main(args: Array<String>) {
    runApplication<TelegramBot>(*args)
}