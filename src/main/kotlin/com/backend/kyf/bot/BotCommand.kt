package com.backend.kyf.bot

import com.backend.kyf.entity.User

enum class BotCommand {
    START {
        override fun description(): String {
            return "Starts conversation with bot"
        }

        override fun nextState(): BotState {
            return BotState.EXPECTING_COMMAND
        }

        override fun generateResponse(user: User): String {
            return "Hello! I'm Know Your Friends Bot. My goal is to keep detailed info about your friends for you"
        }
    },
    CANCEL {
        override fun description(): String {
            return "Cancels all current operations and returns to main menu"
        }

        override fun nextState(): BotState {
            return BotState.EXPECTING_COMMAND
        }

        override fun generateResponse(user: User): String {
            return "Operation cancelled, you're back on main menu"
        }
    },
    HELP {
        override fun description(): String {
            return "Shows a list of all available commands"
        }

        override fun nextState(): BotState {
            return BotState.EXPECTING_COMMAND
        }

        override fun generateResponse(user: User): String {
            val str = values().map { "/${it.toString().lowercase()} - ${it.description()}\n" }
            return "Here you can see a list of all available commands:\n\n${str.joinToString(separator = "")}"
        }
    },
    LIST_FRIENDS {
        override fun description(): String {
            return "Shows a list of your friends"
        }

        override fun nextState(): BotState {
            return BotState.EXPECTING_COMMAND
        }

        override fun generateResponse(user: User): String {
            return if (user.friends == null) {
                "You have no friends :("
            } else {
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

        override fun nextState(): BotState {
            return BotState.EXPECTING_FRIEND_NAME
        }

        override fun generateResponse(user: User): String {
            return "About whom would you like to get some information?"
        }
    },
    ADD_FRIEND {
        override fun description(): String {
            return "Adds new friend"
        }

        override fun nextState(): BotState {
            return BotState.EXPECTING_FRIEND_NAME
        }

        override fun generateResponse(user: User): String {
            return "Enter your friends' name"
        }
    },
    REMOVE_FRIEND {
        override fun description(): String {
            return "Removes existing friend"
        }

        override fun nextState(): BotState {
            return BotState.EXPECTING_FRIEND_NAME
        }

        override fun generateResponse(user: User): String {
            return "Which friend would you like to remove?"
        }
    },
    ADD_FRIENDS_ATTRIBUTE {
        override fun description(): String {
            return "Adds new attribute to a friend"
        }

        override fun nextState(): BotState {
            return BotState.EXPECTING_FRIEND_NAME
        }

        override fun generateResponse(user: User): String {
            return "Whom you want the attribute to be added to?"
        }
    },
    UPDATE_FRIENDS_ATTRIBUTE {
        override fun description(): String {
            return "Updates friend's existing attribute value"
        }

        override fun nextState(): BotState {
            return BotState.EXPECTING_FRIEND_NAME
        }

        override fun generateResponse(user: User): String {
            return "Whose attribute would you like to update?"
        }
    },
    REMOVE_FRIENDS_ATTRIBUTE {
        override fun description(): String {
            return "Removes friend's existing attribute"
        }

        override fun nextState(): BotState {
            return BotState.EXPECTING_FRIEND_NAME
        }

        override fun generateResponse(user: User): String {
            return "From whom would you like to remove an attribute?"
        }
    },
    ADD_GENERAL_ATTRIBUTE {
        override fun description(): String {
            return "Adds new general attribute (will be added to all friends)"
        }

        override fun nextState(): BotState {
            return BotState.EXPECTING_ATTRIBUTE_NAME
        }

        override fun generateResponse(user: User): String {
            return "What attribute would you like to add?"
        }
    },
    REMOVE_GENERAL_ATTRIBUTE {
        override fun description(): String {
            return "Removes existing general attribute (will be removed from all of your friends)"
        }

        override fun nextState(): BotState {
            return BotState.EXPECTING_ATTRIBUTE_NAME
        }

        override fun generateResponse(user: User): String {
            return "What attribute would you like to remove?"
        }
    };

    abstract fun description(): String
    abstract fun nextState(): BotState
    abstract fun generateResponse(user: User): String
}