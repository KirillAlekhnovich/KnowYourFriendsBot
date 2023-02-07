package com.telegram.bot.dto

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard

class ClientResponseDTO(
    val message: String,
    var markup: ReplyKeyboard? = null
)