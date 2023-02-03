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
                    val friend = userRequestService.getFriendByName(telegramBotStateDTO.id, message.text)
                    telegramBotStateDTO.state = EXPECTING_COMMAND
                    val stringBuilder = StringBuilder()
                    for ((name, value) in friend.attributes) {
                        stringBuilder.append("\t$name: $value\n")
                    }
                     return "Friend - ${friend.name}:\n${stringBuilder}"
                }
                BotCommandHandler.ADD_FRIEND -> {
                    telegramBotStateDTO.state = EXPECTING_COMMAND
                    return try {
                        userRequestService.addFriend(telegramBotStateDTO.id, FriendDTO(0, message.text, emptyMap<String, String?>().toMutableMap()))
                        "Friend was successfully added"
                    } catch (e: RuntimeException) {
                        "There is already one ${message.text} in your friends list. Would you mind naming this friend different?"
                    }
                }
                BotCommandHandler.REMOVE_FRIEND -> {
                    val friendId = userRequestService.getFriendByName(telegramBotStateDTO.id, message.text).id
                    userRequestService.removeFriend(telegramBotStateDTO.id, friendId)
                    telegramBotStateDTO.state = EXPECTING_COMMAND
                    return "Friend was successfully removed"
                }
                BotCommandHandler.ADD_FRIENDS_ATTRIBUTE, BotCommandHandler.UPDATE_FRIENDS_ATTRIBUTE, BotCommandHandler.REMOVE_FRIENDS_ATTRIBUTE -> {
                    val friend = userRequestService.getFriendByName(telegramBotStateDTO.id, message.text).id
                    telegramBotStateDTO.storage["Friend id"] = friend.toString()
                    telegramBotStateDTO.state = EXPECTING_ATTRIBUTE_NAME
                    return "Please enter attribute name"
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
                BotCommandHandler.ADD_FRIENDS_ATTRIBUTE, BotCommandHandler.UPDATE_FRIENDS_ATTRIBUTE -> {
                    telegramBotStateDTO.storage["Attribute name"] = message.text
                    telegramBotStateDTO.state = EXPECTING_ATTRIBUTE_VALUE
                    return "Please specify its value"
                }
                BotCommandHandler.REMOVE_FRIENDS_ATTRIBUTE -> {
                    if (!telegramBotStateDTO.storage.containsKey("Friend id")) {
                        telegramBotStateDTO.storage.clear()
                        telegramBotStateDTO.state = EXPECTING_COMMAND
                        return "Friend was not specified"
                    }
                    friendRequestService.deleteAttribute(telegramBotStateDTO.storage["Friend id"]!!.toLong(), message.text)
                    telegramBotStateDTO.state = EXPECTING_COMMAND
                    return "Attribute ${message.text} was successfully removed"
                }
                BotCommandHandler.ADD_GENERAL_ATTRIBUTE -> {
                    userRequestService.addGeneralAttribute(telegramBotStateDTO.id, message.text)
                    telegramBotStateDTO.state = EXPECTING_COMMAND
                    return "Attribute was successfully added"
                }
                BotCommandHandler.REMOVE_GENERAL_ATTRIBUTE -> {
                    userRequestService.removeGeneralAttribute(telegramBotStateDTO.id, message.text)
                    telegramBotStateDTO.state = EXPECTING_COMMAND
                    return "Attribute ${message.text} was successfully removed"
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
            when (telegramBotStateDTO.command) {
                BotCommandHandler.CANCEL -> return cleanup(user, telegramBotStateDTO)
                BotCommandHandler.ADD_FRIENDS_ATTRIBUTE, BotCommandHandler.UPDATE_FRIENDS_ATTRIBUTE -> {
                    if (!telegramBotStateDTO.storage.containsKey("Friend id")
                        || !telegramBotStateDTO.storage.containsKey("Attribute name")
                    ) {
                        telegramBotStateDTO.state = EXPECTING_COMMAND
                        return "Friend or attribute was not specified"
                    }
                    friendRequestService.addAttribute(
                        telegramBotStateDTO.storage["Friend id"]!!.toLong(),
                        AttributeDTO(telegramBotStateDTO.storage["Attribute name"]!!, message.text)
                    )
                    telegramBotStateDTO.state = EXPECTING_COMMAND
                    return "Attribute was successfully added/updated"
                }
                else -> {
                    telegramBotStateDTO.state = EXPECTING_COMMAND
                    return "Invalid command while expecting attribute value"
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