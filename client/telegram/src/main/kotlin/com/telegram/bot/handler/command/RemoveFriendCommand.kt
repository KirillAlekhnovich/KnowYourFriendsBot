package com.telegram.bot.handler.command

import com.telegram.bot.dto.TelegramBotStateDTO
import com.telegram.bot.dto.UserDTO
import com.telegram.bot.dto.getParamFromStorage
import com.telegram.bot.handler.BotState
import com.telegram.bot.service.UserRequestService
import com.telegram.bot.utils.Commands
import com.telegram.bot.utils.CommandsMap
import com.telegram.bot.utils.StorageParams
import org.springframework.stereotype.Component
import javax.inject.Named

@Component
@Named(Commands.REMOVE_FRIEND)
class RemoveFriendCommand(
    private val userRequestService: UserRequestService
) : Command {
    override fun description(): String {
        return "Removes existing friend"
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
            BotState.EXPECTING_COMMAND -> "Which friend would you like to remove?"
            BotState.EXPECTING_FRIEND_NAME -> {
                try {
                    telegramBotState.commandsQueue.add(Commands.LIST_FRIENDS)
                    val friendId = userRequestService.getFriendByName(telegramBotState.userId, message).id
                    userRequestService.removeFriend(telegramBotState.userId, friendId)
                } catch (e: RuntimeException) {
                    e.message!!
                }
            }
            BotState.EXECUTE_USING_STORAGE -> {
                telegramBotState.commandsQueue.add(Commands.LIST_FRIENDS)
                val friendId = telegramBotState.getParamFromStorage(StorageParams.FRIEND_ID).toLong()
                userRequestService.removeFriend(telegramBotState.userId, friendId)
            }
            else -> CommandsMap.get(Commands.UNKNOWN).getMessage(user, message, telegramBotState)
        }
    }
}