package com.telegram.bot.entity

import com.telegram.bot.handler.BotState
import javax.persistence.*


@Entity
data class TelegramBotState(
    @Id
    var userId: Long,

    var currCommand: String,

    var state: BotState,

    @ElementCollection(fetch = FetchType.EAGER)
    var commandsQueue: MutableList<String> = mutableListOf(),

    @ElementCollection(fetch = FetchType.EAGER)
    val storage: MutableMap<String, String>
)