package com.telegram.bot.utils

import com.telegram.bot.handler.BotCommandHandler

object CommandParser {

    private val commands = mapOf(
        "Show friends" to BotCommandHandler.LIST_FRIENDS,
        "Add friend" to BotCommandHandler.ADD_FRIEND,
        "Add attribute" to BotCommandHandler.ADD_GENERAL_ATTRIBUTE,
        "Delete attribute" to BotCommandHandler.REMOVE_GENERAL_ATTRIBUTE,
        "Help" to BotCommandHandler.HELP,
        "Reset" to BotCommandHandler.RESET
    )

    fun String.isCommand(): Boolean {
        return commands.containsKey(this) || enumContains<BotCommandHandler>(this.substring(1).uppercase())
    }

    fun String.toCommand(): BotCommandHandler {
        return try {
            if (commands.containsKey(this)) commands[this]!!
            else enumValueOf(this.substring(1).uppercase())
        } catch (e: IllegalArgumentException) {
            BotCommandHandler.UNKNOWN
        }
    }

    private inline fun <reified T : Enum<T>> enumContains(name: String): Boolean {
        return enumValues<T>().any { it.name == name}
    }
}