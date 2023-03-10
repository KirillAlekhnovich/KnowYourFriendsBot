package com.telegram.bot.handler.command

import com.telegram.bot.dto.*
import com.telegram.bot.handler.BotState
import com.telegram.bot.service.FriendRequestService
import com.telegram.bot.service.UserRequestService
import com.telegram.bot.utils.Commands
import com.telegram.bot.utils.CommandsMap
import com.telegram.bot.utils.StorageParams
import org.springframework.stereotype.Component
import javax.inject.Named

@Component
@Named(Commands.ADD_FRIENDS_ATTRIBUTE)
class AddFriendsAttributeCommand(
    private val friendRequestService: FriendRequestService,
    private val userRequestService: UserRequestService
) : Command {
    override fun description(): String {
        return "Adds new attribute to a friend"
    }

    override fun nextState(botState: TelegramBotStateDTO): BotState {
        return when (botState.state) {
            BotState.EXPECTING_COMMAND -> BotState.EXPECTING_FRIEND_NAME
            BotState.EXPECTING_FRIEND_NAME, BotState.EXECUTE_USING_STORAGE -> BotState.EXPECTING_ATTRIBUTE_NAME
            BotState.EXPECTING_ATTRIBUTE_NAME -> BotState.EXPECTING_ATTRIBUTE_VALUE
            BotState.EXPECTING_ATTRIBUTE_VALUE -> BotState.EXPECTING_COMMAND
            else -> BotState.EXPECTING_COMMAND
        }
    }

    override fun getMessage(user: UserDTO, message: String, telegramBotState: TelegramBotStateDTO): String {
        return when (telegramBotState.state) {
            BotState.EXPECTING_COMMAND -> "Please specify friend's name"
            BotState.EXPECTING_FRIEND_NAME -> {
                try {
                    val friend = userRequestService.getFriendByName(user.id, message)
                    telegramBotState.addParamToStorage(StorageParams.FRIEND_ID, friend.id.toString())
                    "What attribute would you like to add?"
                } catch (e: RuntimeException) {
                    telegramBotState.state = BotState.ERROR
                    e.message!!
                }
            }
            BotState.EXECUTE_USING_STORAGE -> "What attribute would you like to add?"
            BotState.EXPECTING_ATTRIBUTE_NAME -> {
                try {
                    val friendId = telegramBotState.getParamFromStorage(StorageParams.FRIEND_ID).toLong()
                    if (friendRequestService.hasAttribute(friendId, message)) {
                        telegramBotState.state = BotState.ERROR
                        return "Friend already has attribute with name $message"
                    }
                    telegramBotState.addParamToStorage(StorageParams.ATTRIBUTE_NAME, message)
                    "Please specify its value"
                } catch (e: RuntimeException) {
                    telegramBotState.state = BotState.ERROR
                    e.message!!
                }
            }
            BotState.EXPECTING_ATTRIBUTE_VALUE -> {
                try {
                    telegramBotState.commandsQueue.add(Commands.FRIEND_INFO + Commands.STORAGE)
                    val friendId = telegramBotState.getParamFromStorage(StorageParams.FRIEND_ID).toLong()
                    val attributeName = telegramBotState.getParamFromStorage(StorageParams.ATTRIBUTE_NAME)
                    friendRequestService.addAttribute(
                        friendId,
                        AttributeDTO(attributeName, message)
                    )
                } catch (e: RuntimeException) {
                    telegramBotState.state = BotState.ERROR
                    e.message!!
                }
            }
            else -> CommandsMap.get(Commands.UNKNOWN).getMessage(user, message, telegramBotState)
        }
    }
}