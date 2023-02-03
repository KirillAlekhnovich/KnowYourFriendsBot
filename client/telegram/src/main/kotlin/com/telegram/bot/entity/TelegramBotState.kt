package com.telegram.bot.entity

import com.telegram.bot.handler.BotCommandHandler
import com.telegram.bot.handler.BotStateHandler
import javax.persistence.*


@Entity
data class TelegramBotState(
    @Id
    var id: Long,

    var command: BotCommandHandler,

    var state: BotStateHandler,

    @ElementCollection(fetch = FetchType.EAGER)
    val storage: MutableMap<String, String>
)