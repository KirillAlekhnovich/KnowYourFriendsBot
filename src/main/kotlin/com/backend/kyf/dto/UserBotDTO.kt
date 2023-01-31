package com.backend.kyf.dto

import com.backend.kyf.bot.BotCommand
import com.backend.kyf.bot.BotState

class UserBotDTO(
    var id: Long,
    var command: BotCommand,
    var state: BotState,
    var storage: MutableMap<String, String>
)