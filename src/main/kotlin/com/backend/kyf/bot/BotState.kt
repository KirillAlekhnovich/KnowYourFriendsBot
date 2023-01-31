package com.backend.kyf.bot

import com.backend.kyf.dto.AttributeDTO
import com.backend.kyf.dto.FriendDTO
import com.backend.kyf.dto.UserBotDTO
import com.backend.kyf.entity.User
import com.backend.kyf.service.FriendService
import com.backend.kyf.service.UserService
import org.telegram.telegrambots.meta.api.objects.Message

enum class BotState {
    EXPECTING_COMMAND {
        override fun execute(
            user: User, userBotDTO: UserBotDTO, message: Message,
            userService: UserService, friendService: FriendService
        ): String {
            userBotDTO.storage.clear()
            return try {
                val command = enumValueOf<BotCommand>(message.text.substring(1).uppercase())
                userBotDTO.state = command.nextState()
                userBotDTO.command = command
                if (command == BotCommand.RESET) userService.reset(user.id)
                command.generateResponse(user)
            } catch (_: IllegalArgumentException) {
                "I don't know this command. You can check available commands by typing /help"
            }
        }
    },
    EXPECTING_FRIEND_NAME {
        override fun execute(
            user: User, userBotDTO: UserBotDTO, message: Message,
            userService: UserService, friendService: FriendService
        ): String {
            when (userBotDTO.command) {
                BotCommand.CANCEL -> return cleanup(user, userBotDTO)
                BotCommand.FRIEND_INFO -> {
                    val friend = userService.getFriendByName(userBotDTO.id, message.text)
                        ?: return "Friend with name ${message.text} was not found"
                    userBotDTO.state = EXPECTING_COMMAND
                    val stringBuilder = StringBuilder()
                    for ((name, value) in friend.attributes!!) {
                        stringBuilder.append("\t$name: $value\n")
                    }
                    return "Friend - ${friend.name}:\n${stringBuilder}"
                }
                BotCommand.ADD_FRIEND -> {
                    userBotDTO.state = EXPECTING_COMMAND
                    return try {
                        userService.addFriend(userBotDTO.id, FriendDTO(0, message.text, null))
                        "Friend was successfully added"
                    } catch (e: RuntimeException) {
                        "There is already one ${message.text} in your friends list. Would you mind naming this friend different?"
                    }
                }
                BotCommand.REMOVE_FRIEND -> {
                    val friendId = userService.getFriendByName(userBotDTO.id, message.text)?.id
                        ?: return "Friend with name ${message.text} was not found"
                    userService.removeFriend(userBotDTO.id, friendId)
                    userBotDTO.state = EXPECTING_COMMAND
                    return "Friend was successfully removed"
                }
                BotCommand.ADD_FRIENDS_ATTRIBUTE, BotCommand.UPDATE_FRIENDS_ATTRIBUTE, BotCommand.REMOVE_FRIENDS_ATTRIBUTE -> {
                    val friend = userService.getFriendByName(userBotDTO.id, message.text)?.id
                        ?: return "Friend with name ${message.text} was not found"
                    userBotDTO.storage["Friend id"] = friend.toString()
                    userBotDTO.state = EXPECTING_ATTRIBUTE_NAME
                    return "Please enter attribute name"
                }
                else -> {
                    userBotDTO.state = EXPECTING_COMMAND
                    return "Invalid command while expecting friend name"
                }
            }
        }
    },
    EXPECTING_ATTRIBUTE_NAME {
        override fun execute(
            user: User, userBotDTO: UserBotDTO, message: Message,
            userService: UserService, friendService: FriendService
        ): String {
            when (userBotDTO.command) {
                BotCommand.CANCEL -> return cleanup(user, userBotDTO)
                BotCommand.ADD_FRIENDS_ATTRIBUTE, BotCommand.UPDATE_FRIENDS_ATTRIBUTE -> {
                    userBotDTO.storage["Attribute name"] = message.text
                    userBotDTO.state = EXPECTING_ATTRIBUTE_VALUE
                    return "Please specify its value"
                }
                BotCommand.REMOVE_FRIENDS_ATTRIBUTE -> {
                    if (!userBotDTO.storage.containsKey("Friend id")) {
                        userBotDTO.storage.clear()
                        userBotDTO.state = EXPECTING_COMMAND
                        return "Friend was not specified"
                    }
                    friendService.deleteAttributeFromFriend(userBotDTO.storage["Friend id"]!!.toLong(), message.text)
                    userBotDTO.state = EXPECTING_COMMAND
                    return "Attribute ${message.text} was successfully removed"
                }
                BotCommand.ADD_GENERAL_ATTRIBUTE -> {
                    userService.addAttributeToAllFriends(userBotDTO.id, message.text)
                    userBotDTO.state = EXPECTING_COMMAND
                    return "Attribute was successfully added"
                }
                BotCommand.REMOVE_GENERAL_ATTRIBUTE -> {
                    userService.removeAttributeFromAllFriends(userBotDTO.id, message.text)
                    userBotDTO.state = EXPECTING_COMMAND
                    return "Attribute ${message.text} was successfully removed"
                }
                else -> {
                    userBotDTO.state = EXPECTING_COMMAND
                    return "Invalid command while expecting attribute name"
                }
            }
        }
    },
    EXPECTING_ATTRIBUTE_VALUE {
        override fun execute(
            user: User, userBotDTO: UserBotDTO, message: Message,
            userService: UserService, friendService: FriendService
        ): String {
            when (userBotDTO.command) {
                BotCommand.CANCEL -> return cleanup(user, userBotDTO)
                BotCommand.ADD_FRIENDS_ATTRIBUTE, BotCommand.UPDATE_FRIENDS_ATTRIBUTE -> {
                    if (!userBotDTO.storage.containsKey("Friend id")
                        || !userBotDTO.storage.containsKey("Attribute name")
                    ) {
                        userBotDTO.state = EXPECTING_COMMAND
                        return "Friend or attribute was not specified"
                    }
                    friendService.addAttributeToFriend(
                        userBotDTO.storage["Friend id"]!!.toLong(),
                        AttributeDTO(userBotDTO.storage["Attribute name"]!!, message.text)
                    )
                    userBotDTO.state = EXPECTING_COMMAND
                    return "Attribute was successfully added/updated"
                }
                else -> {
                    userBotDTO.state = EXPECTING_COMMAND
                    return "Invalid command while expecting attribute value"
                }
            }
        }
    };

    abstract fun execute(
        user: User, userBotDTO: UserBotDTO, message: Message,
        userService: UserService, friendService: FriendService
    ): String

    fun cleanup(user: User, userBotDTO: UserBotDTO): String {
        userBotDTO.storage.clear()
        userBotDTO.state = EXPECTING_COMMAND
        userBotDTO.command = BotCommand.CANCEL
        return BotCommand.CANCEL.generateResponse(user)
    }
}