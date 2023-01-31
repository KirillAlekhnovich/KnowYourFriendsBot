package com.backend.kyf.dto

import com.backend.kyf.bot.BotCommand
import com.backend.kyf.bot.BotState

data class UserBotDTO(
    var id: Long,
    var command: BotCommand,
    var state: BotState,
    val storage: MutableMap<String, String>
)