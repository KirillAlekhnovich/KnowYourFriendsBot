package com.telegram.bot.handler.command

import com.telegram.bot.dto.TelegramBotStateDTO
import com.telegram.bot.dto.UserDTO
import com.telegram.bot.handler.BotState
import com.telegram.bot.service.UserRequestService
import com.telegram.bot.utils.Commands
import com.telegram.bot.utils.CommandsMap
import org.springframework.stereotype.Component
import javax.inject.Named

@Component
@Named(Commands.REMOVE_GENERAL_ATTRIBUTE)
class RemoveGeneralAttributeCommand(
    private val userRequestService: UserRequestService
) : Command {
    override fun description(): String {
        return "Removes existing general attribute (will be removed from all of your friends)"
    }

    override fun nextState(botState: TelegramBotStateDTO): BotState {
        return when (botState.state) {
            BotState.EXPECTING_COMMAND -> BotState.EXPECTING_ATTRIBUTE_NAME
            BotState.EXPECTING_ATTRIBUTE_NAME -> BotState.EXPECTING_COMMAND
            else -> BotState.EXPECTING_COMMAND
        }
    }

    override fun getMessage(user: UserDTO, message: String, telegramBotState: TelegramBotStateDTO): String {
        return when (telegramBotState.state) {
            BotState.EXPECTING_COMMAND -> "What attribute would you like to remove?"
            BotState.EXPECTING_ATTRIBUTE_NAME -> userRequestService.removeGeneralAttribute(
                telegramBotState.userId,
                message
            )
            else -> CommandsMap.get(Commands.UNKNOWN).getMessage(user, message, telegramBotState)
        }
    }
}