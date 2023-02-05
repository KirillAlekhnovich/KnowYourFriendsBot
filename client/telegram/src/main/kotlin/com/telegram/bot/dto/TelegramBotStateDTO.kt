package com.telegram.bot.dto

import com.telegram.bot.handler.BotCommandHandler
import com.telegram.bot.handler.BotState

data class TelegramBotStateDTO(
    var id: Long,
    var command: BotCommandHandler,
    var state: BotState,
    val storage: MutableMap<String, String>
)