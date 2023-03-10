package com.telegram.bot.handler.command

import com.telegram.bot.dto.ClientResponseDTO
import com.telegram.bot.dto.TelegramBotStateDTO
import com.telegram.bot.dto.UserDTO
import com.telegram.bot.handler.BotState
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard

@Component
interface Command {
    fun description(): String

    fun nextState(botState: TelegramBotStateDTO): BotState

    fun execute(
        user: UserDTO,
        message: String,
        telegramBotState: TelegramBotStateDTO
    ): ClientResponseDTO {
        return ClientResponseDTO(getMessage(user, message, telegramBotState), getButtons(telegramBotState))
    }

    fun getMessage(
        user: UserDTO,
        message: String,
        telegramBotState: TelegramBotStateDTO
    ): String

    fun getButtons(botState: TelegramBotStateDTO): ReplyKeyboard? = null
}