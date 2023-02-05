package com.telegram.bot.handler


enum class BotState {
    EXPECTING_COMMAND,
    EXPECTING_FRIEND_NAME,
    EXPECTING_ATTRIBUTE_NAME,
    EXPECTING_ATTRIBUTE_VALUE,
    ERROR
}