package com.telegram.bot.handler.command

import com.telegram.bot.dto.ClientResponseDTO
import com.telegram.bot.dto.UserDTO
import com.telegram.bot.handler.BotState
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard

/**
 * Interface that represents a command.
 */
@Component
interface Command {
    /**
     * Returns command description.
     */
    fun description(): String

    /**
     * Returns next bot state.
     */
    fun nextState(userId: Long): BotState

    /**
     * Executing command.
     */
    fun execute(user: UserDTO, message: String): ClientResponseDTO {
        return ClientResponseDTO(getMessage(user, message), getButtons(user.id))
    }

    /**
     * Returns message that will be sent to user.
     */
    fun getMessage(user: UserDTO, message: String): String

    /**
     * Returns buttons that will be sent to user.
     */
    fun getButtons(userId: Long): ReplyKeyboard? = null
}