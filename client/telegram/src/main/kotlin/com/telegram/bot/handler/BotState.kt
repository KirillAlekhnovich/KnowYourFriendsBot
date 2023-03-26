package com.telegram.bot.handler

/**
 * Enum that represents current bot state.
 */
enum class BotState {
    EXPECTING_COMMAND,
    EXPECTING_APPROVAL,
    EXPECTING_FRIEND_NAME,
    EXPECTING_NEW_FRIEND_NAME,
    EXPECTING_ATTRIBUTE_NAME,
    EXPECTING_ATTRIBUTE_VALUE,
    EXECUTE_USING_STORAGE,
    ERROR,
}