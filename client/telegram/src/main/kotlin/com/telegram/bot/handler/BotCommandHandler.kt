package com.telegram.bot.handler

import com.telegram.bot.dto.UserDTO

enum class BotCommandHandler {
    START {
        override fun description(): String {
            return "Starts conversation with bot"
        }

        override fun nextState(): BotStateHandler {
            return BotStateHandler.EXPECTING_COMMAND
        }

        override fun generateResponse(user: UserDTO): String {
            return "Hello! I'm Know Your Friends Bot. My goal is to keep detailed info about your friends for you"
        }
    },
    CANCEL {
        override fun description(): String {
            return "Cancels all current operations and returns to main menu"
        }

        override fun nextState(): BotStateHandler {
            return BotStateHandler.EXPECTING_COMMAND
        }

        override fun generateResponse(user: UserDTO): String {
            return "Operation cancelled, you're back on main menu"
        }
    },
    HELP {
        override fun description(): String {
            return "Shows a list of all available commands"
        }

        override fun nextState(): BotStateHandler {
            return BotStateHandler.EXPECTING_COMMAND
        }

        override fun generateResponse(user: UserDTO): String {
            val str = values().map { "/${it.toString().lowercase()} - ${it.description()}\n" }
            return "Here you can see a list of all available commands:\n\n${str.joinToString(separator = "")}"
        }
    },
    RESET {
        override fun description(): String {
            return "Resets your profile. Removes all your friends and general attributes"
        }

        override fun nextState(): BotStateHandler {
            return BotStateHandler.EXPECTING_COMMAND
        }

        override fun generateResponse(user: UserDTO): String {
            return "Your profile has been reset"
        }

    },
    LIST_FRIENDS {
        override fun description(): String {
            return "Shows a list of your friends"
        }

        override fun nextState(): BotStateHandler {
            return BotStateHandler.EXPECTING_COMMAND
        }

        override fun generateResponse(user: UserDTO): String {
            return if (user.friends.isEmpty()) "You have no friends :("
            else {
                val stringBuilder = StringBuilder()
                var index = 1
                user.friends.map { stringBuilder.append("$index. ${it.name}\n"); index++ }
                stringBuilder.toString()
            }
        }
    },
    FRIEND_INFO {
        override fun description(): String {
            return "Shows info about specific friend"
        }

        override fun nextState(): BotStateHandler {
            return BotStateHandler.EXPECTING_FRIEND_NAME
        }

        override fun generateResponse(user: UserDTO): String {
            return "About whom would you like to get some information?"
        }
    },
    ADD_FRIEND {
        override fun description(): String {
            return "Adds new friend"
        }

        override fun nextState(): BotStateHandler {
            return BotStateHandler.EXPECTING_FRIEND_NAME
        }

        override fun generateResponse(user: UserDTO): String {
            return "Enter your friends' name"
        }
    },
    REMOVE_FRIEND {
        override fun description(): String {
            return "Removes existing friend"
        }

        override fun nextState(): BotStateHandler {
            return BotStateHandler.EXPECTING_FRIEND_NAME
        }

        override fun generateResponse(user: UserDTO): String {
            return "Which friend would you like to remove?"
        }
    },
    ADD_FRIENDS_ATTRIBUTE {
        override fun description(): String {
            return "Adds new attribute to a friend"
        }

        override fun nextState(): BotStateHandler {
            return BotStateHandler.EXPECTING_FRIEND_NAME
        }

        override fun generateResponse(user: UserDTO): String {
            return "Whom you want the attribute to be added to?"
        }
    },
    UPDATE_FRIENDS_ATTRIBUTE {
        override fun description(): String {
            return "Updates friend's existing attribute value"
        }

        override fun nextState(): BotStateHandler {
            return BotStateHandler.EXPECTING_FRIEND_NAME
        }

        override fun generateResponse(user: UserDTO): String {
            return "Whose attribute would you like to update?"
        }
    },
    REMOVE_FRIENDS_ATTRIBUTE {
        override fun description(): String {
            return "Removes friend's existing attribute"
        }

        override fun nextState(): BotStateHandler {
            return BotStateHandler.EXPECTING_FRIEND_NAME
        }

        override fun generateResponse(user: UserDTO): String {
            return "From whom would you like to remove an attribute?"
        }
    },
    ADD_GENERAL_ATTRIBUTE {
        override fun description(): String {
            return "Adds new general attribute (will be added to all friends)"
        }

        override fun nextState(): BotStateHandler {
            return BotStateHandler.EXPECTING_ATTRIBUTE_NAME
        }

        override fun generateResponse(user: UserDTO): String {
            return "What attribute would you like to add?"
        }
    },
    REMOVE_GENERAL_ATTRIBUTE {
        override fun description(): String {
            return "Removes existing general attribute (will be removed from all of your friends)"
        }

        override fun nextState(): BotStateHandler {
            return BotStateHandler.EXPECTING_ATTRIBUTE_NAME
        }

        override fun generateResponse(user: UserDTO): String {
            return "What attribute would you like to remove?"
        }
    };

    abstract fun description(): String
    abstract fun nextState(): BotStateHandler
    abstract fun generateResponse(user: UserDTO): String
}