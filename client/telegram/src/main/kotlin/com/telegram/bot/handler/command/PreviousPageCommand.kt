package com.telegram.bot.handler.command

import com.telegram.bot.dto.*
import com.telegram.bot.handler.BotState
import com.telegram.bot.utils.Commands
import com.telegram.bot.utils.CommandsMap
import com.telegram.bot.utils.MessageType
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import javax.inject.Named

@Component
@Named(Commands.PREVIOUS_PAGE)
class PreviousPageCommand : Command {
    override fun description(): String {
        return "Shows previous page of friends list"
    }

    override fun nextState(botState: TelegramBotStateDTO): BotState {
        return BotState.EXPECTING_COMMAND
    }

    override fun execute(user: UserDTO, message: String, telegramBotState: TelegramBotStateDTO): ClientResponseDTO {
        return ClientResponseDTO(
            getMessage(user, message, telegramBotState),
            getButtons(telegramBotState),
            MessageType.EDIT
        )
    }

    override fun getMessage(user: UserDTO, message: String, telegramBotState: TelegramBotStateDTO): String {
        try {
            telegramBotState.incrementPage(-1)
        } catch (e: Exception) {
            return "Something went wrong"
        }
        return CommandsMap.get(Commands.LIST_FRIENDS).getMessage(user, message, telegramBotState)
    }

    override fun getButtons(botState: TelegramBotStateDTO): ReplyKeyboard? {
        return CommandsMap.get(Commands.LIST_FRIENDS).getButtons(botState)
    }
}