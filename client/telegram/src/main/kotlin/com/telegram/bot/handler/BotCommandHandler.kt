package com.telegram.bot.handler

import com.telegram.bot.dto.*
import com.telegram.bot.service.FriendRequestService
import com.telegram.bot.service.UserRequestService
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import java.util.ArrayList

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
            telegramBotState: TelegramBotStateDTO,
            message: String,
            userRequestService: UserRequestService,
            friendRequestService: FriendRequestService
        ): List<ClientResponseDTO> {
            return listOf(
                ClientResponseDTO(
                    "Hello! I'm Know Your Friends Bot. My goal is to keep detailed info about your friends for you. " +
                            "Please, select one of the options below to continue", generateButtons(
                        telegramBotState.state,
                        user, telegramBotState, friendRequestService, userRequestService
                    )
                )
            )
        }

        override fun generateButtons(
            botState: BotState,
            user: UserDTO,
            telegramBotState: TelegramBotStateDTO,
            friendRequestService: FriendRequestService,
            userRequestService: UserRequestService
        ): ReplyKeyboard {
            return MAIN_MENU.generateButtons(
                botState,
                user,
                telegramBotState,
                friendRequestService,
                userRequestService
            )!!
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
            telegramBotState: TelegramBotStateDTO,
            message: String,
            userRequestService: UserRequestService,
            friendRequestService: FriendRequestService
        ): List<ClientResponseDTO> {
            return listOf(
                ClientResponseDTO(
                    "You're back on main menu. Select one of the options below",
                    generateButtons(
                        telegramBotState.state,
                        user,
                        telegramBotState,
                        friendRequestService,
                        userRequestService
                    )
                )
            )
        }

        override fun generateButtons(
            botState: BotState,
            user: UserDTO,
            telegramBotState: TelegramBotStateDTO,
            friendRequestService: FriendRequestService,
            userRequestService: UserRequestService
        ): ReplyKeyboard {
            val buttons: MutableList<KeyboardRow> = ArrayList()
            val row = KeyboardRow()

            row.add(createKeyboardButton("Show friends"))
            row.add(createKeyboardButton("Add attribute"))
            row.add(createKeyboardButton("Help"))

            buttons.add(createRowInstance(row).toKeyboardRow())
            row.clear()

            row.add(createKeyboardButton("Add friend"))
            row.add(createKeyboardButton("Remove attribute"))
            row.add(createKeyboardButton("Reset profile"))

            buttons.add(createRowInstance(row).toKeyboardRow())

            return createKeyboardMarkup(buttons)
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
            telegramBotState: TelegramBotStateDTO,
            message: String,
            userRequestService: UserRequestService,
            friendRequestService: FriendRequestService
        ): List<ClientResponseDTO> {
            val str = values().filter { it != UNKNOWN }.map { "/${it.toString().lowercase()} - ${it.description()}\n" }
            return listOf(
                ClientResponseDTO(
                    "Here you can see a list of all available commands:\n\n${
                        str.joinToString(
                            separator = ""
                        )
                    }"
                )
            )
        }

        override fun generateButtons(
            botState: BotState,
            user: UserDTO,
            telegramBotState: TelegramBotStateDTO,
            friendRequestService: FriendRequestService,
            userRequestService: UserRequestService
        ): ReplyKeyboard? {
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
            telegramBotState: TelegramBotStateDTO,
            message: String,
            userRequestService: UserRequestService,
            friendRequestService: FriendRequestService
        ): List<ClientResponseDTO> {
            telegramBotState.storage.clear()
            userRequestService.resetUser(user.id)
            return listOf(
                ClientResponseDTO(
                    "Your profile has been reset",
                    generateButtons(
                        telegramBotState.state,
                        user,
                        telegramBotState,
                        friendRequestService,
                        userRequestService
                    )
                )
            )
        }

        override fun generateButtons(
            botState: BotState,
            user: UserDTO,
            telegramBotState: TelegramBotStateDTO,
            friendRequestService: FriendRequestService,
            userRequestService: UserRequestService
        ): ReplyKeyboard? {
            return MAIN_MENU.generateButtons(botState, user, telegramBotState, friendRequestService, userRequestService)
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
            telegramBotState: TelegramBotStateDTO,
            message: String,
            userRequestService: UserRequestService,
            friendRequestService: FriendRequestService
        ): List<ClientResponseDTO> {
            val friends = userRequestService.getFriendNames(user.id)
            return if (friends.isEmpty()) listOf(
                ClientResponseDTO(
                    "You have no friends :(",
                    generateButtons(
                        telegramBotState.state,
                        user,
                        telegramBotState,
                        friendRequestService,
                        userRequestService
                    )
                )
            )
            else {
                val stringBuilder = StringBuilder()
                stringBuilder.append("List of your friends:\n\n")
                var index = 1
                friends.map { stringBuilder.append("$index. ${it}\n"); index++ }
                listOf(
                    ClientResponseDTO(
                        stringBuilder.toString(),
                        generateButtons(
                            telegramBotState.state,
                            user,
                            telegramBotState,
                            friendRequestService,
                            userRequestService
                        )
                    )
                )
            }
        }

        override fun generateButtons(
            botState: BotState,
            user: UserDTO,
            telegramBotState: TelegramBotStateDTO,
            friendRequestService: FriendRequestService,
            userRequestService: UserRequestService
        ): ReplyKeyboard {
            val buttons: MutableList<MutableList<InlineKeyboardButton>> = ArrayList()
            val row: MutableList<InlineKeyboardButton> = ArrayList()

            row.add(createInlineButton("Previous page", "/previous_page"))
            row.add(createInlineButton("Next page", "/next_page"))
            if (userRequestService.getFriendNames(user.id).size > 10) buttons.add(createRowInstance(row))
            row.clear()

            row.add(createInlineButton("Add friend", ADD_FRIEND.toCommand()))
            row.add(createInlineButton("Remove friend", REMOVE_FRIEND.toCommand()))
            buttons.add(createRowInstance(row))
            row.clear()

            row.add(createInlineButton("Get friend info", FRIEND_INFO.toCommand()))
            buttons.add(createRowInstance(row))

            return createInlineMarkup(buttons)
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
                BotState.EXECUTE_USING_STORAGE -> BotState.EXPECTING_COMMAND
                else -> BotState.EXPECTING_COMMAND
            }
        }

        override fun execute(
            user: UserDTO,
            telegramBotState: TelegramBotStateDTO,
            message: String,
            userRequestService: UserRequestService,
            friendRequestService: FriendRequestService
        ): List<ClientResponseDTO> {
            return when (telegramBotState.state) {
                BotState.EXPECTING_COMMAND -> listOf(ClientResponseDTO("Please enter the name of the friend"))
                BotState.EXPECTING_FRIEND_NAME -> {
                    try {
                        val friend = userRequestService.getFriendByName(user.id, message)
                        addFriendIdToStorage(friend.id, telegramBotState)
                        listOf(
                            ClientResponseDTO(
                                parseFriendInfo(friend.id, friendRequestService),
                                generateButtons(
                                    telegramBotState.state,
                                    user,
                                    telegramBotState,
                                    friendRequestService,
                                    userRequestService
                                )
                            )
                        )
                    } catch (e: RuntimeException) {
                        listOf(ClientResponseDTO(e.message!!))
                    }
                }
                BotState.EXECUTE_USING_STORAGE -> {
                    try {
                        val friendId = getFriendIdFromStorage(telegramBotState).toLong()
                        listOf(
                            ClientResponseDTO(
                                parseFriendInfo(friendId, friendRequestService),
                                generateButtons(
                                    telegramBotState.state,
                                    user,
                                    telegramBotState,
                                    friendRequestService,
                                    userRequestService
                                )
                            )
                        )
                    } catch (e: RuntimeException) {
                        listOf(ClientResponseDTO(e.message!!))
                    }
                }
                else -> UNKNOWN.execute(user, telegramBotState, message, userRequestService, friendRequestService)
            }
        }

        override fun generateButtons(
            botState: BotState,
            user: UserDTO,
            telegramBotState: TelegramBotStateDTO,
            friendRequestService: FriendRequestService,
            userRequestService: UserRequestService
        ): ReplyKeyboard {
            val buttons: MutableList<MutableList<InlineKeyboardButton>> = ArrayList()
            val row: MutableList<InlineKeyboardButton> = ArrayList()

            row.add(createInlineButton("Add attribute", ADD_FRIENDS_ATTRIBUTE.toCommand()))
            row.add(createInlineButton("Update attribute", UPDATE_FRIENDS_ATTRIBUTE.toCommand()))
            buttons.add(createRowInstance(row))
            row.clear()

            row.add(createInlineButton("Remove attribute", REMOVE_FRIENDS_ATTRIBUTE.toCommand()))
            row.add(createInlineButton("Remove friend", REMOVE_FRIEND.toCommand()))
            buttons.add(createRowInstance(row))
            row.clear()

            row.add(createInlineButton("Back to list", LIST_FRIENDS.toCommand()))
            buttons.add(createRowInstance(row))

            return createInlineMarkup(buttons)
        }

        private fun parseFriendInfo(friendId: Long, friendRequestService: FriendRequestService): String {
            val friend = friendRequestService.getFriend(friendId)
            val attributes = friendRequestService.getAttributes(friendId)
            val stringBuilder = StringBuilder()
            for ((name, value) in attributes) {
                stringBuilder.append("\t$name: $value\n")
            }
            if (stringBuilder.isEmpty()) stringBuilder.append("\tFriend has no attributes")
            return "Info about your friend *${friend.name}*:\n${stringBuilder}"
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
            telegramBotState: TelegramBotStateDTO,
            message: String,
            userRequestService: UserRequestService,
            friendRequestService: FriendRequestService
        ): List<ClientResponseDTO> {
            return when (telegramBotState.state) {
                BotState.EXPECTING_COMMAND -> listOf(ClientResponseDTO("Please enter the name of the friend"))
                BotState.EXPECTING_FRIEND_NAME -> {
                    listOf(
                        ClientResponseDTO(
                            userRequestService.addFriend(
                                telegramBotState.id,
                                FriendDTO(0, message, emptyMap<String, String?>().toMutableMap())
                            )
                        ),
                        LIST_FRIENDS.execute(user, telegramBotState, message, userRequestService, friendRequestService)
                            .first()
                    )
                }
                else -> UNKNOWN.execute(user, telegramBotState, message, userRequestService, friendRequestService)
            }
        }

        override fun generateButtons(
            botState: BotState,
            user: UserDTO,
            telegramBotState: TelegramBotStateDTO,
            friendRequestService: FriendRequestService,
            userRequestService: UserRequestService
        ): ReplyKeyboard? {
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
            telegramBotState: TelegramBotStateDTO,
            message: String,
            userRequestService: UserRequestService,
            friendRequestService: FriendRequestService
        ): List<ClientResponseDTO> {
            return when (telegramBotState.state) {
                BotState.EXPECTING_COMMAND -> {
                    if (telegramBotState.storage.containsKey("Friend id")) {
                        telegramBotState.state = BotState.EXECUTE_USING_STORAGE
                        REMOVE_FRIEND.execute(user, telegramBotState, message, userRequestService, friendRequestService)
                    } else listOf(ClientResponseDTO("Which friend would you like to remove?"))
                }
                BotState.EXPECTING_FRIEND_NAME -> {
                    try {
                        val friendId = userRequestService.getFriendByName(telegramBotState.id, message).id
                        listOf(
                            ClientResponseDTO(userRequestService.removeFriend(telegramBotState.id, friendId)),
                            LIST_FRIENDS.execute(
                                user,
                                telegramBotState, message, userRequestService, friendRequestService
                            ).first()
                        )
                    } catch (e: RuntimeException) {
                        listOf(ClientResponseDTO(e.message!!))
                    }
                }
                BotState.EXECUTE_USING_STORAGE -> {
                    val friendId = telegramBotState.storage["Friend id"]!!.toLong()
                    listOf(
                        ClientResponseDTO(userRequestService.removeFriend(telegramBotState.id, friendId)),
                        LIST_FRIENDS.execute(
                            user,
                            telegramBotState, message, userRequestService, friendRequestService
                        ).first()
                    )
                }
                else -> UNKNOWN.execute(user, telegramBotState, message, userRequestService, friendRequestService)
            }
        }

        override fun generateButtons(
            botState: BotState,
            user: UserDTO,
            telegramBotState: TelegramBotStateDTO,
            friendRequestService: FriendRequestService,
            userRequestService: UserRequestService
        ): ReplyKeyboard? {
            return null
        }
    },
    ADD_FRIENDS_ATTRIBUTE {
        override fun description(): String {
            return "Adds new attribute to a friend"
        }

        override fun nextState(currState: BotState): BotState {
            return when (currState) {
                BotState.EXPECTING_COMMAND -> BotState.EXPECTING_ATTRIBUTE_NAME
                BotState.EXPECTING_ATTRIBUTE_NAME -> BotState.EXPECTING_ATTRIBUTE_VALUE
                BotState.EXPECTING_ATTRIBUTE_VALUE -> BotState.EXPECTING_COMMAND
                else -> BotState.EXPECTING_COMMAND
            }
        }

        override fun execute(
            user: UserDTO,
            telegramBotState: TelegramBotStateDTO,
            message: String,
            userRequestService: UserRequestService,
            friendRequestService: FriendRequestService
        ): List<ClientResponseDTO> {
            when (telegramBotState.state) {
                BotState.EXPECTING_COMMAND -> return listOf(ClientResponseDTO("What attribute would you like to add?"))
                BotState.EXPECTING_ATTRIBUTE_NAME -> {
                    return try {
                        val friendId = getFriendIdFromStorage(telegramBotState).toLong()
                        if (friendRequestService.hasAttribute(friendId, message)) {
                            telegramBotState.state = BotState.ERROR
                            listOf(ClientResponseDTO("Friend already has attribute with name ${message}. If you want to change it's value, please use /update_attribute command"))
                        }
                        telegramBotState.storage["Attribute name"] = message
                        listOf(ClientResponseDTO("Please specify its value"))
                    } catch (e: RuntimeException) {
                        telegramBotState.state = BotState.ERROR
                        listOf(ClientResponseDTO(e.message!!))
                    }
                }
                BotState.EXPECTING_ATTRIBUTE_VALUE -> {
                    return try {
                        val friendId = getFriendIdFromStorage(telegramBotState).toLong()
                        val attributeName = getAttributeNameFromStorage(telegramBotState)
                        telegramBotState.state = BotState.EXECUTE_USING_STORAGE
                        listOf(
                            ClientResponseDTO(
                                friendRequestService.addAttribute(
                                    friendId,
                                    AttributeDTO(attributeName, message)
                                )
                            ),
                            FRIEND_INFO.execute(
                                user,
                                telegramBotState,
                                message,
                                userRequestService,
                                friendRequestService
                            ).first()
                        )
                    } catch (e: RuntimeException) {
                        telegramBotState.state = BotState.ERROR
                        listOf(ClientResponseDTO(e.message!!))
                    }
                }
                else -> return UNKNOWN.execute(
                    user,
                    telegramBotState,
                    message,
                    userRequestService,
                    friendRequestService
                )
            }
        }

        override fun generateButtons(
            botState: BotState,
            user: UserDTO,
            telegramBotState: TelegramBotStateDTO,
            friendRequestService: FriendRequestService,
            userRequestService: UserRequestService
        ): ReplyKeyboard? {
            return null
        }
    },
    UPDATE_FRIENDS_ATTRIBUTE {
        override fun description(): String {
            return "Updates friend's existing attribute value"
        }

        override fun nextState(currState: BotState): BotState {
            return when (currState) {
                BotState.EXPECTING_COMMAND -> BotState.EXPECTING_ATTRIBUTE_NAME
                BotState.EXPECTING_ATTRIBUTE_NAME -> BotState.EXPECTING_ATTRIBUTE_VALUE
                BotState.EXPECTING_ATTRIBUTE_VALUE -> BotState.EXPECTING_COMMAND
                else -> BotState.EXPECTING_COMMAND
            }
        }

        override fun execute(
            user: UserDTO,
            telegramBotState: TelegramBotStateDTO,
            message: String,
            userRequestService: UserRequestService,
            friendRequestService: FriendRequestService
        ): List<ClientResponseDTO> {
            when (telegramBotState.state) {
                BotState.EXPECTING_COMMAND -> return listOf(
                    ClientResponseDTO(
                        "What attribute would you like to update?",
                        generateButtons(
                            BotState.EXPECTING_COMMAND,
                            user,
                            telegramBotState,
                            friendRequestService,
                            userRequestService
                        )
                    )
                )
                BotState.EXPECTING_ATTRIBUTE_NAME -> {
                    return try {
                        val friendId = getFriendIdFromStorage(telegramBotState).toLong()
                        if (!friendRequestService.hasAttribute(friendId, message)) {
                            telegramBotState.state = BotState.ERROR
                            listOf(ClientResponseDTO("Friend doesn't have attribute with name ${message}. If you want to add it, please use /add_attribute command"))
                        } else {
                            telegramBotState.storage["Attribute name"] = message
                            listOf(ClientResponseDTO("Please specify its new value"))
                        }
                    } catch (e: RuntimeException) {
                        telegramBotState.state = BotState.ERROR
                        listOf(ClientResponseDTO(e.message!!))
                    }
                }
                BotState.EXPECTING_ATTRIBUTE_VALUE -> {
                    return try {
                        val friendId = getFriendIdFromStorage(telegramBotState).toLong()
                        val attributeName = getAttributeNameFromStorage(telegramBotState)
                        telegramBotState.state = BotState.EXECUTE_USING_STORAGE
                        listOf(
                            ClientResponseDTO(
                                friendRequestService.updateAttribute(
                                    friendId,
                                    AttributeDTO(attributeName, message)
                                )
                            ),
                            FRIEND_INFO.execute(
                                user,
                                telegramBotState, message, userRequestService, friendRequestService
                            ).first()
                        )
                    } catch (e: RuntimeException) {
                        telegramBotState.state = BotState.ERROR
                        listOf(ClientResponseDTO(e.message!!))
                    }
                }
                else -> return UNKNOWN.execute(
                    user,
                    telegramBotState,
                    message,
                    userRequestService,
                    friendRequestService
                )
            }
        }

        override fun generateButtons(
            botState: BotState,
            user: UserDTO,
            telegramBotState: TelegramBotStateDTO,
            friendRequestService: FriendRequestService,
            userRequestService: UserRequestService
        ): ReplyKeyboard? {
            return createAttributesMarkup(telegramBotState, friendRequestService)
        }
    },
    REMOVE_FRIENDS_ATTRIBUTE {
        override fun description(): String {
            return "Removes friend's existing attribute"
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
            telegramBotState: TelegramBotStateDTO,
            message: String,
            userRequestService: UserRequestService,
            friendRequestService: FriendRequestService
        ): List<ClientResponseDTO> {
            return when (telegramBotState.state) {
                BotState.EXPECTING_COMMAND -> listOf(
                    ClientResponseDTO(
                        "What attribute would you like to remove?",
                        generateButtons(
                            BotState.EXPECTING_COMMAND,
                            user,
                            telegramBotState,
                            friendRequestService,
                            userRequestService
                        )
                    )
                )
                BotState.EXPECTING_ATTRIBUTE_NAME -> {
                    return try {
                        val friendId = getFriendIdFromStorage(telegramBotState).toLong()
                        telegramBotState.state = BotState.EXECUTE_USING_STORAGE
                        listOf(
                            ClientResponseDTO(friendRequestService.deleteAttribute(friendId, message)),
                            FRIEND_INFO.execute(
                                user,
                                telegramBotState, message, userRequestService, friendRequestService
                            ).first()
                        )
                    } catch (e: RuntimeException) {
                        telegramBotState.state = BotState.ERROR
                        listOf(ClientResponseDTO(e.message!!))
                    }
                }
                else -> UNKNOWN.execute(user, telegramBotState, message, userRequestService, friendRequestService)
            }
        }

        override fun generateButtons(
            botState: BotState,
            user: UserDTO,
            telegramBotState: TelegramBotStateDTO,
            friendRequestService: FriendRequestService,
            userRequestService: UserRequestService
        ): ReplyKeyboard? {
            return createAttributesMarkup(telegramBotState, friendRequestService)
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
            telegramBotState: TelegramBotStateDTO,
            message: String,
            userRequestService: UserRequestService,
            friendRequestService: FriendRequestService
        ): List<ClientResponseDTO> {
            return when (telegramBotState.state) {
                BotState.EXPECTING_COMMAND -> listOf(ClientResponseDTO("What attribute would you like to add?"))
                BotState.EXPECTING_ATTRIBUTE_NAME -> listOf(
                    ClientResponseDTO(
                        userRequestService.addGeneralAttribute(
                            telegramBotState.id, message
                        )
                    )
                )
                else -> UNKNOWN.execute(user, telegramBotState, message, userRequestService, friendRequestService)
            }
        }

        override fun generateButtons(
            botState: BotState,
            user: UserDTO,
            telegramBotState: TelegramBotStateDTO,
            friendRequestService: FriendRequestService,
            userRequestService: UserRequestService
        ): ReplyKeyboard? {
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
            telegramBotState: TelegramBotStateDTO,
            message: String,
            userRequestService: UserRequestService,
            friendRequestService: FriendRequestService
        ): List<ClientResponseDTO> {
            return when (telegramBotState.state) {
                BotState.EXPECTING_COMMAND -> listOf(ClientResponseDTO("What attribute would you like to remove?"))
                BotState.EXPECTING_ATTRIBUTE_NAME -> listOf(
                    ClientResponseDTO(
                        userRequestService.removeGeneralAttribute(
                            telegramBotState.id, message
                        )
                    )
                )
                else -> UNKNOWN.execute(user, telegramBotState, message, userRequestService, friendRequestService)
            }
        }

        override fun generateButtons(
            botState: BotState,
            user: UserDTO,
            telegramBotState: TelegramBotStateDTO,
            friendRequestService: FriendRequestService,
            userRequestService: UserRequestService
        ): ReplyKeyboard? {
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
            telegramBotState: TelegramBotStateDTO,
            message: String,
            userRequestService: UserRequestService,
            friendRequestService: FriendRequestService
        ): List<ClientResponseDTO> {
            return listOf(ClientResponseDTO("I don't know this command. You can check available commands by typing /help"))
        }

        override fun generateButtons(
            botState: BotState,
            user: UserDTO,
            telegramBotState: TelegramBotStateDTO,
            friendRequestService: FriendRequestService,
            userRequestService: UserRequestService
        ): ReplyKeyboard? {
            return null
        }
    };

    abstract fun description(): String

    abstract fun nextState(currState: BotState): BotState

    abstract fun execute(
        user: UserDTO,
        telegramBotState: TelegramBotStateDTO,
        message: String,
        userRequestService: UserRequestService,
        friendRequestService: FriendRequestService
    ): List<ClientResponseDTO>

    abstract fun generateButtons(
        botState: BotState,
        user: UserDTO,
        telegramBotState: TelegramBotStateDTO,
        friendRequestService: FriendRequestService,
        userRequestService: UserRequestService
    ): ReplyKeyboard?

    fun createInlineButton(text: String, callback: String? = null): InlineKeyboardButton {
        val button = InlineKeyboardButton()
        button.text = text
        button.callbackData = callback ?: text
        return button
    }

    fun createKeyboardButton(text: String): KeyboardButton {
        val button = KeyboardButton()
        button.text = text
        return button
    }

    fun <T> createRowInstance(buttons: MutableList<T>): MutableList<T> {
        val row: MutableList<T> = ArrayList()
        row.addAll(buttons)
        return row
    }

    fun createInlineMarkup(buttons: List<MutableList<InlineKeyboardButton>>): ReplyKeyboard {
        val markup = InlineKeyboardMarkup()
        markup.keyboard = buttons
        return markup
    }

    fun createKeyboardMarkup(buttons: List<KeyboardRow>): ReplyKeyboard {
        val markup = ReplyKeyboardMarkup()
        markup.keyboard = buttons
        markup.resizeKeyboard = true
        return markup
    }

    fun createAttributesMarkup(
        telegramBotState: TelegramBotStateDTO,
        friendRequestService: FriendRequestService
    ): ReplyKeyboard? {
        return try {
            val friendId = getFriendIdFromStorage(telegramBotState).toLong()
            val friendAttributes = friendRequestService.getAttributeNames(friendId)
            val buttons: MutableList<MutableList<InlineKeyboardButton>> = ArrayList()
            val row: MutableList<InlineKeyboardButton> = ArrayList()
            val numberOfColumns = 2
            var split = 1
            friendAttributes.forEach {
                row.add(createInlineButton(it))
                if (split % numberOfColumns == 0) {
                    buttons.add(createRowInstance(row))
                    row.clear()
                }
                split++
            }
            row.add(createInlineButton("Back", "${FRIEND_INFO.toCommand()}_storage"))
            if (row.isNotEmpty()) buttons.add(row)
            createInlineMarkup(buttons)
        } catch (e: Exception) {
            null
        }
    }

    fun addFriendIdToStorage(friendId: Long, telegramBotStateDTO: TelegramBotStateDTO) {
        telegramBotStateDTO.storage["Friend id"] = friendId.toString()
    }

    private fun getParameterFromStorage(parameter: String, telegramBotStateDTO: TelegramBotStateDTO): String {
        if (!telegramBotStateDTO.storage.containsKey(parameter)) {
            throw RuntimeException("I can't determine $parameter. Please try again")
        }
        return telegramBotStateDTO.storage[parameter]!!
    }

    fun getFriendIdFromStorage(telegramBotStateDTO: TelegramBotStateDTO): String {
        return getParameterFromStorage("Friend id", telegramBotStateDTO)
    }

    fun getAttributeNameFromStorage(telegramBotStateDTO: TelegramBotStateDTO): String {
        return getParameterFromStorage("Attribute name", telegramBotStateDTO)
    }
}

fun BotCommandHandler.toCommand(): String {
    return "/${this.name.lowercase()}"
}

fun MutableList<KeyboardButton>.toKeyboardRow(): KeyboardRow {
    val row = KeyboardRow()
    row.addAll(this)
    return row
}