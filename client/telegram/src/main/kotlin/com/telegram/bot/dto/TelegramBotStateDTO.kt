package com.telegram.bot.dto

import com.telegram.bot.handler.BotCommandHandler
import com.telegram.bot.handler.BotStateHandler

data class TelegramBotStateDTO(
    var id: Long,
    var command: BotCommandHandler,
    var state: BotStateHandler,
    val storage: MutableMap<String, String>
)