package com.backend.kyf.entity

import com.backend.kyf.bot.BotCommand
import com.backend.kyf.bot.BotState
import javax.persistence.*


@Entity
data class UserBot(
    @Id
    var id: Long,

    var command: BotCommand,

    var state: BotState,

    @ElementCollection(fetch = FetchType.EAGER)
    val storage: MutableMap<String, String>
)