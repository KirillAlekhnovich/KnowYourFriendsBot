package com.telegram.bot.handler.command

import com.telegram.bot.dto.ClientResponseDTO
import com.telegram.bot.dto.UserDTO
import com.telegram.bot.handler.BotState
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard

@Component
interface Command {
    fun description(): String

    fun nextState(userId: Long): BotState

    fun execute(user: UserDTO, message: String): ClientResponseDTO {
        return ClientResponseDTO(getMessage(user, message), getButtons(user.id))
    }

    fun getMessage(user: UserDTO, message: String): String

    fun getButtons(userId: Long): ReplyKeyboard? = null
}