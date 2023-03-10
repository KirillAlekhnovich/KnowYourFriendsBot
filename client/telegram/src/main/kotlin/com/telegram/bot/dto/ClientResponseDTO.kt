package com.telegram.bot.dto

import com.telegram.bot.utils.MessageType
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard

class ClientResponseDTO(
    val message: String,
    var markup: ReplyKeyboard? = null,
    val type: MessageType = MessageType.SEND
)