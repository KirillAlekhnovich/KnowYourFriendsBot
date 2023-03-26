package com.telegram.bot.utils

/**
 * Enum that represents all Redis keys which are used in client.
 */
enum class RedisParams {
    ACCESS_TOKEN,
    COMMAND,
    STATE,
    COMMANDS_QUEUE,
    FRIEND_ID,
    FRIEND_NAME,
    ATTRIBUTE_NAME,
    CALLBACK_MESSAGE_ID,
    CURRENT_PAGE,
}