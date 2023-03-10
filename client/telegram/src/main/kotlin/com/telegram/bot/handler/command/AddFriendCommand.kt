package com.telegram.bot.handler.command

import com.telegram.bot.dto.FriendDTO
import com.telegram.bot.dto.TelegramBotStateDTO
import com.telegram.bot.dto.UserDTO
import com.telegram.bot.handler.BotState
import com.telegram.bot.service.UserRequestService
import com.telegram.bot.utils.Commands
import com.telegram.bot.utils.CommandsMap
import org.springframework.stereotype.Component
import javax.inject.Named

@Component
@Named(Commands.ADD_FRIEND)
class AddFriendCommand(
    private val userRequestService: UserRequestService
) : Command {
    override fun description(): String {
        return "Adds new friend"
    }

    override fun nextState(botState: TelegramBotStateDTO): BotState {
        return when (botState.state) {
            BotState.EXPECTING_COMMAND -> BotState.EXPECTING_FRIEND_NAME
            BotState.EXPECTING_FRIEND_NAME -> BotState.EXPECTING_COMMAND
            else -> BotState.EXPECTING_COMMAND
        }
    }

    override fun getMessage(user: UserDTO, message: String, telegramBotState: TelegramBotStateDTO): String {
        return when (telegramBotState.state) {
            BotState.EXPECTING_COMMAND -> "Please enter the name of the friend"
            BotState.EXPECTING_FRIEND_NAME -> {
                telegramBotState.commandsQueue.add(Commands.LIST_FRIENDS)
                userRequestService.addFriend(
                    telegramBotState.userId,
                    FriendDTO(0, message, emptyMap<String, String?>().toMutableMap())
                )
            }
            else -> CommandsMap.get(Commands.UNKNOWN).getMessage(user, message, telegramBotState)
        }
    }
}