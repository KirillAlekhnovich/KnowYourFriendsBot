package com.telegram.bot.handler

import com.telegram.bot.dto.AttributeDTO
import com.telegram.bot.dto.FriendDTO
import com.telegram.bot.dto.TelegramBotStateDTO
import com.telegram.bot.dto.UserDTO
import com.telegram.bot.service.FriendRequestService
import com.telegram.bot.service.UserRequestService
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard

enum class BotCommandHandler {
    START {
        override fun description(): String {
            return "Starts conversation with bot"
        }

        override fun nextState(currState: BotState): BotState {
            return BotState.EXPECTING_COMMAND
        }

        override fun execute(
            user: UserDTO,
            telegramBotStateDTO: TelegramBotStateDTO,
            message: String,
            userRequestService: UserRequestService,
            friendRequestService: FriendRequestService
        ): String {
            return "Hello! I'm Know Your Friends Bot. My goal is to keep detailed info about your friends for you. " +
                    "Please, select one of the options below to continue"
        }

        override fun generateButtons(botState: BotState): ReplyKeyboard? {
            return null
        }
    },
    MAIN_MENU {
        override fun description(): String {
            return "Returns to main menu"
        }

        override fun nextState(currState: BotState): BotState {
            return BotState.EXPECTING_COMMAND
        }

        override fun execute(
            user: UserDTO,
            telegramBotStateDTO: TelegramBotStateDTO,
            message: String,
            userRequestService: UserRequestService,
            friendRequestService: FriendRequestService
        ): String {
            telegramBotStateDTO.storage.clear()
            return "You're back on main menu. Select one of the options below"
        }

        override fun generateButtons(botState: BotState): ReplyKeyboard? {
            return null
        }
    },
    HELP {
        override fun description(): String {
            return "Shows a list of all available commands"
        }

        override fun nextState(currState: BotState): BotState {
            return BotState.EXPECTING_COMMAND
        }

        override fun execute(
            user: UserDTO,
            telegramBotStateDTO: TelegramBotStateDTO,
            message: String,
            userRequestService: UserRequestService,
            friendRequestService: FriendRequestService
        ): String {
            val str = values().filter { it != UNKNOWN }.map { "/${it.toString().lowercase()} - ${it.description()}\n" }
            return "Here you can see a list of all available commands:\n\n${str.joinToString(separator = "")}"
        }

        override fun generateButtons(botState: BotState): ReplyKeyboard? {
            return null
        }
    },
    RESET {
        override fun description(): String {
            return "Resets your profile. Removes all your friends and general attributes"
        }

        override fun nextState(currState: BotState): BotState {
            return BotState.EXPECTING_COMMAND
        }

        override fun execute(
            user: UserDTO,
            telegramBotStateDTO: TelegramBotStateDTO,
            message: String,
            userRequestService: UserRequestService,
            friendRequestService: FriendRequestService
        ): String {
            userRequestService.resetUser(user.id)
            return "Your profile has been reset"
        }

        override fun generateButtons(botState: BotState): ReplyKeyboard? {
            return null
        }
    },
    LIST_FRIENDS {
        override fun description(): String {
            return "Shows a list of your friends"
        }

        override fun nextState(currState: BotState): BotState {
            return BotState.EXPECTING_COMMAND
        }

        override fun execute(
            user: UserDTO,
            telegramBotStateDTO: TelegramBotStateDTO,
            message: String,
            userRequestService: UserRequestService,
            friendRequestService: FriendRequestService
        ): String {
            return if (user.friends.isEmpty()) "You have no friends :("
            else {
                val stringBuilder = StringBuilder()
                var index = 1
                user.friends.map { stringBuilder.append("$index. ${it.name}\n"); index++ }
                stringBuilder.toString()
            }
        }

        override fun generateButtons(botState: BotState): ReplyKeyboard? {
            return null
        }
    },
    FRIEND_INFO {
        override fun description(): String {
            return "Shows info about specific friend"
        }

        override fun nextState(currState: BotState): BotState {
            return when (currState) {
                BotState.EXPECTING_COMMAND -> BotState.EXPECTING_FRIEND_NAME
                BotState.EXPECTING_FRIEND_NAME -> BotState.EXPECTING_COMMAND
                else -> BotState.EXPECTING_COMMAND
            }
        }

        override fun execute(
            user: UserDTO,
            telegramBotStateDTO: TelegramBotStateDTO,
            message: String,
            userRequestService: UserRequestService,
            friendRequestService: FriendRequestService
        ): String {
            return when (telegramBotStateDTO.state) {
                BotState.EXPECTING_COMMAND -> "Please enter the name of the friend"
                BotState.EXPECTING_FRIEND_NAME -> {
                    try {
                        val friend = userRequestService.getFriendByName(user.id, message)
                        friendRequestService.getFriendInfo(friend.id)
                    } catch (e: RuntimeException) {
                        e.message!!
                    }
                }
                else -> UNKNOWN.execute(user, telegramBotStateDTO, message, userRequestService, friendRequestService)
            }
        }

        override fun generateButtons(botState: BotState): ReplyKeyboard? {
            return null
        }
    },
    ADD_FRIEND {
        override fun description(): String {
            return "Adds new friend"
        }

        override fun nextState(currState: BotState): BotState {
            return when (currState) {
                BotState.EXPECTING_COMMAND -> BotState.EXPECTING_FRIEND_NAME
                BotState.EXPECTING_FRIEND_NAME -> BotState.EXPECTING_COMMAND
                else -> BotState.EXPECTING_COMMAND
            }
        }

        override fun execute(
            user: UserDTO,
            telegramBotStateDTO: TelegramBotStateDTO,
            message: String,
            userRequestService: UserRequestService,
            friendRequestService: FriendRequestService
        ): String {
            return when (telegramBotStateDTO.state) {
                BotState.EXPECTING_COMMAND -> "Please enter the name of the friend"
                BotState.EXPECTING_FRIEND_NAME -> userRequestService.addFriend(
                    telegramBotStateDTO.id,
                    FriendDTO(0, message, emptyMap<String, String?>().toMutableMap())
                )
                else -> UNKNOWN.execute(user, telegramBotStateDTO, message, userRequestService, friendRequestService)
            }
        }

        override fun generateButtons(botState: BotState): ReplyKeyboard? {
            return null
        }
    },
    REMOVE_FRIEND {
        override fun description(): String {
            return "Removes existing friend"
        }

        override fun nextState(currState: BotState): BotState {
            return when (currState) {
                BotState.EXPECTING_COMMAND -> BotState.EXPECTING_FRIEND_NAME
                BotState.EXPECTING_FRIEND_NAME -> BotState.EXPECTING_COMMAND
                else -> BotState.EXPECTING_COMMAND
            }
        }

        override fun execute(
            user: UserDTO,
            telegramBotStateDTO: TelegramBotStateDTO,
            message: String,
            userRequestService: UserRequestService,
            friendRequestService: FriendRequestService
        ): String {
            when (telegramBotStateDTO.state) {
                BotState.EXPECTING_COMMAND -> return "Which friend would you like to remove?"
                BotState.EXPECTING_FRIEND_NAME -> {
                    return try {
                        val friendId = userRequestService.getFriendByName(telegramBotStateDTO.id, message).id
                        return userRequestService.removeFriend(telegramBotStateDTO.id, friendId)
                    } catch (e: RuntimeException) {
                        e.message!!
                    }
                }
                else -> return UNKNOWN.execute(user, telegramBotStateDTO, message, userRequestService, friendRequestService)
            }
        }

        override fun generateButtons(botState: BotState): ReplyKeyboard? {
            return null
        }
    },
    ADD_FRIENDS_ATTRIBUTE {
        override fun description(): String {
            return "Adds new attribute to a friend"
        }

        override fun nextState(currState: BotState): BotState {
            return when (currState) {
                BotState.EXPECTING_COMMAND -> BotState.EXPECTING_FRIEND_NAME
                BotState.EXPECTING_FRIEND_NAME -> BotState.EXPECTING_ATTRIBUTE_NAME
                BotState.EXPECTING_ATTRIBUTE_NAME -> BotState.EXPECTING_ATTRIBUTE_VALUE
                BotState.EXPECTING_ATTRIBUTE_VALUE -> BotState.EXPECTING_COMMAND
                else -> BotState.EXPECTING_COMMAND
            }
        }

        override fun execute(
            user: UserDTO,
            telegramBotStateDTO: TelegramBotStateDTO,
            message: String,
            userRequestService: UserRequestService,
            friendRequestService: FriendRequestService
        ): String {
            when (telegramBotStateDTO.state) {
                BotState.EXPECTING_COMMAND -> return "Whom you want the attribute to be added to?"
                BotState.EXPECTING_FRIEND_NAME -> return addFriendIdToStorage(telegramBotStateDTO, message, userRequestService)
                BotState.EXPECTING_ATTRIBUTE_NAME -> {
                    if (friendRequestService.hasAttribute(
                            telegramBotStateDTO.storage["Friend id"]!!.toLong(),
                            message
                        )
                    ) {
                        telegramBotStateDTO.state = BotState.ERROR
                        return "Friend already has attribute with name ${message}. If you want to change it's value, please use /update_attribute command"
                    }
                    telegramBotStateDTO.storage["Attribute name"] = message
                    return "Please specify its value"
                }
                BotState.EXPECTING_ATTRIBUTE_VALUE -> return friendRequestService.addAttribute(
                    telegramBotStateDTO.storage["Friend id"]!!.toLong(),
                    AttributeDTO(telegramBotStateDTO.storage["Attribute name"]!!, message)
                )
                else -> return UNKNOWN.execute(user, telegramBotStateDTO, message, userRequestService, friendRequestService)
            }
        }

        override fun generateButtons(botState: BotState): ReplyKeyboard? {
            return null
        }
    },
    UPDATE_FRIENDS_ATTRIBUTE {
        override fun description(): String {
            return "Updates friend's existing attribute value"
        }

        override fun nextState(currState: BotState): BotState {
            return when (currState) {
                BotState.EXPECTING_COMMAND -> BotState.EXPECTING_FRIEND_NAME
                BotState.EXPECTING_FRIEND_NAME -> BotState.EXPECTING_ATTRIBUTE_NAME
                BotState.EXPECTING_ATTRIBUTE_NAME -> BotState.EXPECTING_ATTRIBUTE_VALUE
                BotState.EXPECTING_ATTRIBUTE_VALUE -> BotState.EXPECTING_COMMAND
                else -> BotState.EXPECTING_COMMAND
            }
        }

        override fun execute(
            user: UserDTO,
            telegramBotStateDTO: TelegramBotStateDTO,
            message: String,
            userRequestService: UserRequestService,
            friendRequestService: FriendRequestService
        ): String {
            when (telegramBotStateDTO.state) {
                BotState.EXPECTING_COMMAND -> return "Whom's attribute would you like to update?"
                BotState.EXPECTING_FRIEND_NAME -> return addFriendIdToStorage(telegramBotStateDTO, message, userRequestService)
                BotState.EXPECTING_ATTRIBUTE_NAME -> {
                    if (!friendRequestService.hasAttribute(telegramBotStateDTO.storage["Friend id"]!!.toLong(), message)) {
                        telegramBotStateDTO.state = BotState.ERROR
                        return "Friend doesn't have attribute with name ${message}. If you want to add it, please use /add_attribute command"
                    }
                    telegramBotStateDTO.storage["Attribute name"] = message
                    return "Please specify its new value"
                }
                BotState.EXPECTING_ATTRIBUTE_VALUE -> return friendRequestService.updateAttribute(
                    telegramBotStateDTO.storage["Friend id"]!!.toLong(),
                    AttributeDTO(telegramBotStateDTO.storage["Attribute name"]!!, message)
                )
                else -> return UNKNOWN.execute(user, telegramBotStateDTO, message, userRequestService, friendRequestService)
            }
        }

        override fun generateButtons(botState: BotState): ReplyKeyboard? {
            return null
        }
    },
    REMOVE_FRIENDS_ATTRIBUTE {
        override fun description(): String {
            return "Removes friend's existing attribute"
        }

        override fun nextState(currState: BotState): BotState {
            return when (currState) {
                BotState.EXPECTING_COMMAND -> BotState.EXPECTING_FRIEND_NAME
                BotState.EXPECTING_FRIEND_NAME -> BotState.EXPECTING_ATTRIBUTE_NAME
                BotState.EXPECTING_ATTRIBUTE_NAME -> BotState.EXPECTING_COMMAND
                else -> BotState.EXPECTING_COMMAND
            }
        }

        override fun execute(
            user: UserDTO,
            telegramBotStateDTO: TelegramBotStateDTO,
            message: String,
            userRequestService: UserRequestService,
            friendRequestService: FriendRequestService
        ): String {
            return when (telegramBotStateDTO.state) {
                BotState.EXPECTING_COMMAND -> "Which friend's attribute would you like to remove?"
                BotState.EXPECTING_FRIEND_NAME -> addFriendIdToStorage(telegramBotStateDTO, message, userRequestService)
                BotState.EXPECTING_ATTRIBUTE_NAME -> friendRequestService.deleteAttribute(telegramBotStateDTO.storage["Friend id"]!!.toLong(), message)
                else -> UNKNOWN.execute(user, telegramBotStateDTO, message, userRequestService, friendRequestService)
            }
        }

        override fun generateButtons(botState: BotState): ReplyKeyboard? {
            return null
        }
    },
    ADD_GENERAL_ATTRIBUTE {
        override fun description(): String {
            return "Adds new general attribute (will be added to all friends)"
        }

        override fun nextState(currState: BotState): BotState {
            return when (currState) {
                BotState.EXPECTING_COMMAND -> BotState.EXPECTING_ATTRIBUTE_NAME
                BotState.EXPECTING_ATTRIBUTE_NAME -> BotState.EXPECTING_COMMAND
                else -> BotState.EXPECTING_COMMAND
            }
        }

        override fun execute(
            user: UserDTO,
            telegramBotStateDTO: TelegramBotStateDTO,
            message: String,
            userRequestService: UserRequestService,
            friendRequestService: FriendRequestService
        ): String {
            return when (telegramBotStateDTO.state) {
                BotState.EXPECTING_COMMAND -> "What attribute would you like to add?"
                BotState.EXPECTING_ATTRIBUTE_NAME -> userRequestService.addGeneralAttribute(telegramBotStateDTO.id, message)
                else -> UNKNOWN.execute(user, telegramBotStateDTO, message, userRequestService, friendRequestService)
            }
        }

        override fun generateButtons(botState: BotState): ReplyKeyboard? {
            return null
        }
    },
    REMOVE_GENERAL_ATTRIBUTE {
        override fun description(): String {
            return "Removes existing general attribute (will be removed from all of your friends)"
        }

        override fun nextState(currState: BotState): BotState {
            return when (currState) {
                BotState.EXPECTING_COMMAND -> BotState.EXPECTING_ATTRIBUTE_NAME
                BotState.EXPECTING_ATTRIBUTE_NAME -> BotState.EXPECTING_COMMAND
                else -> BotState.EXPECTING_COMMAND
            }
        }

        override fun execute(
            user: UserDTO,
            telegramBotStateDTO: TelegramBotStateDTO,
            message: String,
            userRequestService: UserRequestService,
            friendRequestService: FriendRequestService
        ): String {
            return when (telegramBotStateDTO.state) {
                BotState.EXPECTING_COMMAND -> "What attribute would you like to remove?"
                BotState.EXPECTING_ATTRIBUTE_NAME -> userRequestService.removeGeneralAttribute(telegramBotStateDTO.id, message)
                else -> UNKNOWN.execute(user, telegramBotStateDTO, message, userRequestService, friendRequestService)
            }
        }

        override fun generateButtons(botState: BotState): ReplyKeyboard? {
            return null
        }
    },
    UNKNOWN {
        override fun description(): String {
            return "Unknown command"
        }

        override fun nextState(currState: BotState): BotState {
            return BotState.EXPECTING_COMMAND
        }

        override fun execute(
            user: UserDTO,
            telegramBotStateDTO: TelegramBotStateDTO,
            message: String,
            userRequestService: UserRequestService,
            friendRequestService: FriendRequestService
        ): String {
            return "I don't know this command. You can check available commands by typing /help"
        }

        override fun generateButtons(botState: BotState): ReplyKeyboard? {
            return null
        }
    };

    abstract fun description(): String

    abstract fun nextState(currState: BotState): BotState

    abstract fun execute(
        user: UserDTO,
        telegramBotStateDTO: TelegramBotStateDTO,
        message: String,
        userRequestService: UserRequestService,
        friendRequestService: FriendRequestService
    ): String

    abstract fun generateButtons(botState: BotState): ReplyKeyboard?

    fun addFriendIdToStorage(telegramBotStateDTO: TelegramBotStateDTO, message: String, userRequestService: UserRequestService): String {
        return try {
            val friend = userRequestService.getFriendByName(telegramBotStateDTO.id, message).id
            telegramBotStateDTO.storage["Friend id"] = friend.toString()
            "Please enter attribute name"
        } catch (e: RuntimeException) {
            telegramBotStateDTO.state = BotState.ERROR
            e.message!!
        }
    }
}