package com.telegram.bot.handler.command

import com.telegram.bot.dto.*
import com.telegram.bot.handler.BotState
import com.telegram.bot.handler.Buttons
import com.telegram.bot.service.FriendRequestService
import com.telegram.bot.service.UserRequestService
import com.telegram.bot.utils.Commands
import com.telegram.bot.utils.CommandsMap
import com.telegram.bot.utils.StorageParams
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import javax.inject.Named

@Component
@Named(Commands.REMOVE_FRIENDS_ATTRIBUTE)
class RemoveFriendsAttributeCommand(
    private val friendRequestService: FriendRequestService,
    private val userRequestService: UserRequestService
) : Command {
    override fun description(): String {
        return "Removes friend's existing attribute"
    }

    override fun nextState(botState: TelegramBotStateDTO): BotState {
        return when (botState.state) {
            BotState.EXPECTING_COMMAND -> BotState.EXPECTING_FRIEND_NAME
            BotState.EXPECTING_FRIEND_NAME, BotState.EXECUTE_USING_STORAGE -> BotState.EXPECTING_ATTRIBUTE_NAME
            BotState.EXPECTING_ATTRIBUTE_NAME -> BotState.EXPECTING_COMMAND
            else -> BotState.EXPECTING_COMMAND
        }
    }

    override fun getMessage(user: UserDTO, message: String, telegramBotState: TelegramBotStateDTO): String {
        return when (telegramBotState.state) {
            BotState.EXPECTING_COMMAND -> "Which friend would you like to remove attribute from?"
            BotState.EXPECTING_FRIEND_NAME -> {
                return try {
                    val friend = userRequestService.getFriendByName(user.id, message)
                    telegramBotState.addParamToStorage(StorageParams.FRIEND_ID, friend.id.toString())
                    "What attribute would you like to remove?"
                } catch (e: RuntimeException) {
                    telegramBotState.state = BotState.ERROR
                    e.message!!
                }
            }
            BotState.EXECUTE_USING_STORAGE -> "What attribute would you like to remove?"
            BotState.EXPECTING_ATTRIBUTE_NAME -> {
                return try {
                    telegramBotState.commandsQueue.add(Commands.FRIEND_INFO + Commands.STORAGE)
                    val friendId = telegramBotState.getParamFromStorage(StorageParams.FRIEND_ID).toLong()
                    friendRequestService.deleteAttribute(friendId, message)
                } catch (e: RuntimeException) {
                    telegramBotState.state = BotState.ERROR
                    e.message!!
                }
            }
            else -> CommandsMap.get(Commands.UNKNOWN).getMessage(user, message, telegramBotState)
        }
    }

    override fun getButtons(botState: TelegramBotStateDTO): ReplyKeyboard? {
        return Buttons.createAttributesMarkup(botState, friendRequestService)
    }
}