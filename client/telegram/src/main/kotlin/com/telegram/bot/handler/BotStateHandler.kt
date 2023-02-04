package com.telegram.bot.handler

import com.telegram.bot.dto.AttributeDTO
import com.telegram.bot.dto.FriendDTO
import com.telegram.bot.dto.TelegramBotStateDTO
import com.telegram.bot.dto.UserDTO
import com.telegram.bot.service.FriendRequestService
import com.telegram.bot.service.UserRequestService
import org.telegram.telegrambots.meta.api.objects.Message

enum class BotStateHandler {
    EXPECTING_COMMAND {
        override fun execute(
            user: UserDTO,
            telegramBotStateDTO: TelegramBotStateDTO,
            message: Message,
            userRequestService: UserRequestService,
            friendRequestService: FriendRequestService
        ): String {
            telegramBotStateDTO.storage.clear()
            return try {
                val command = enumValueOf<BotCommandHandler>(message.text.substring(1).uppercase())
                telegramBotStateDTO.state = command.nextState()
                telegramBotStateDTO.command = command
                if (command == BotCommandHandler.RESET) userRequestService.resetUser(user.id)
                command.generateResponse(user)
            } catch (_: IllegalArgumentException) {
                "I don't know this command. You can check available commands by typing /help"
            }
        }
    },
    EXPECTING_FRIEND_NAME {
        override fun execute(
            user: UserDTO,
            telegramBotStateDTO: TelegramBotStateDTO,
            message: Message,
            userRequestService: UserRequestService,
            friendRequestService: FriendRequestService
        ): String {
            when (telegramBotStateDTO.command) {
                BotCommandHandler.CANCEL -> return cleanup(user, telegramBotStateDTO)
                BotCommandHandler.FRIEND_INFO -> {
                    telegramBotStateDTO.state = EXPECTING_COMMAND
                    return try {
                        val friend = userRequestService.getFriendByName(user.id, message.text)
                        friendRequestService.getFriendInfo(friend.id)
                    } catch (e: RuntimeException) {
                        e.message!!
                    }
                }
                BotCommandHandler.ADD_FRIEND -> {
                    telegramBotStateDTO.state = EXPECTING_COMMAND
                    return userRequestService.addFriend(telegramBotStateDTO.id, FriendDTO(0, message.text, emptyMap<String, String?>().toMutableMap()))
                }
                BotCommandHandler.REMOVE_FRIEND -> {
                    telegramBotStateDTO.state = EXPECTING_COMMAND
                    return try {
                        val friendId = userRequestService.getFriendByName(telegramBotStateDTO.id, message.text).id
                        return userRequestService.removeFriend(telegramBotStateDTO.id, friendId)
                    } catch (e: RuntimeException) {
                        e.message!!
                    }
                }
                BotCommandHandler.ADD_FRIENDS_ATTRIBUTE, BotCommandHandler.UPDATE_FRIENDS_ATTRIBUTE, BotCommandHandler.REMOVE_FRIENDS_ATTRIBUTE -> {
                    return try {
                        val friend = userRequestService.getFriendByName(telegramBotStateDTO.id, message.text).id
                        telegramBotStateDTO.storage["Friend id"] = friend.toString()
                        telegramBotStateDTO.state = EXPECTING_ATTRIBUTE_NAME
                        "Please enter attribute name"
                    } catch (e: RuntimeException) {
                        telegramBotStateDTO.state = EXPECTING_COMMAND
                        e.message!!
                    }
                }
                else -> {
                    telegramBotStateDTO.state = EXPECTING_COMMAND
                    return "Invalid command while expecting friend name"
                }
            }
        }
    },
    EXPECTING_ATTRIBUTE_NAME {
        override fun execute(
            user: UserDTO,
            telegramBotStateDTO: TelegramBotStateDTO,
            message: Message,
            userRequestService: UserRequestService,
            friendRequestService: FriendRequestService
        ): String {
            when (telegramBotStateDTO.command) {
                BotCommandHandler.CANCEL -> return cleanup(user, telegramBotStateDTO)
                BotCommandHandler.ADD_FRIENDS_ATTRIBUTE -> {
                    if (friendRequestService.hasAttribute(telegramBotStateDTO.storage["Friend id"]!!.toLong(), message.text)) {
                        telegramBotStateDTO.state = EXPECTING_COMMAND
                        return "Friend already has attribute with name ${message.text}. If you want to change it's value, please use /update_attribute command"
                    }
                    telegramBotStateDTO.storage["Attribute name"] = message.text
                    telegramBotStateDTO.state = EXPECTING_ATTRIBUTE_VALUE
                    return "Please specify its value"
                }
                BotCommandHandler.UPDATE_FRIENDS_ATTRIBUTE -> {
                    if (!friendRequestService.hasAttribute(telegramBotStateDTO.storage["Friend id"]!!.toLong(), message.text)) {
                        telegramBotStateDTO.state = EXPECTING_COMMAND
                        return "Friend doesn't have attribute with name ${message.text}. If you want to add it, please use /add_attribute command"
                    }
                    telegramBotStateDTO.storage["Attribute name"] = message.text
                    telegramBotStateDTO.state = EXPECTING_ATTRIBUTE_VALUE
                    return "Please specify its new value"
                }
                BotCommandHandler.REMOVE_FRIENDS_ATTRIBUTE -> {
                    telegramBotStateDTO.state = EXPECTING_COMMAND
                    return friendRequestService.deleteAttribute(telegramBotStateDTO.storage["Friend id"]!!.toLong(), message.text)
                }
                BotCommandHandler.ADD_GENERAL_ATTRIBUTE -> {
                    telegramBotStateDTO.state = EXPECTING_COMMAND
                    return userRequestService.addGeneralAttribute(telegramBotStateDTO.id, message.text)
                }
                BotCommandHandler.REMOVE_GENERAL_ATTRIBUTE -> {
                    telegramBotStateDTO.state = EXPECTING_COMMAND
                    return userRequestService.removeGeneralAttribute(telegramBotStateDTO.id, message.text)
                }
                else -> {
                    telegramBotStateDTO.state = EXPECTING_COMMAND
                    return "Invalid command while expecting attribute name"
                }
            }
        }
    },
    EXPECTING_ATTRIBUTE_VALUE {
        override fun execute(
            user: UserDTO,
            telegramBotStateDTO: TelegramBotStateDTO,
            message: Message,
            userRequestService: UserRequestService,
            friendRequestService: FriendRequestService
        ): String {
            return when (telegramBotStateDTO.command) {
                BotCommandHandler.CANCEL -> cleanup(user, telegramBotStateDTO)
                BotCommandHandler.ADD_FRIENDS_ATTRIBUTE -> {
                    telegramBotStateDTO.state = EXPECTING_COMMAND
                    return friendRequestService.addAttribute(
                        telegramBotStateDTO.storage["Friend id"]!!.toLong(),
                        AttributeDTO(telegramBotStateDTO.storage["Attribute name"]!!, message.text)
                    )
                }
                BotCommandHandler.UPDATE_FRIENDS_ATTRIBUTE -> {
                    telegramBotStateDTO.state = EXPECTING_COMMAND
                    return friendRequestService.updateAttribute(
                        telegramBotStateDTO.storage["Friend id"]!!.toLong(),
                        AttributeDTO(telegramBotStateDTO.storage["Attribute name"]!!, message.text)
                    )
                }
                else -> {
                    telegramBotStateDTO.state = EXPECTING_COMMAND
                    "Invalid command while expecting attribute value"
                }
            }
        }
    };

    abstract fun execute(
        user: UserDTO,
        telegramBotStateDTO: TelegramBotStateDTO,
        message: Message,
        userRequestService: UserRequestService,
        friendRequestService: FriendRequestService
    ): String

    fun cleanup(user: UserDTO, telegramBotStateDTO: TelegramBotStateDTO): String {
        telegramBotStateDTO.storage.clear()
        telegramBotStateDTO.state = EXPECTING_COMMAND
        telegramBotStateDTO.command = BotCommandHandler.CANCEL
        return BotCommandHandler.CANCEL.generateResponse(user)
    }
}