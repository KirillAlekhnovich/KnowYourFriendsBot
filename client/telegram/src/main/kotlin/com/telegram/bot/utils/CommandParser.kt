package com.telegram.bot.utils

import com.telegram.bot.handler.BotCommandHandler
import com.telegram.bot.handler.BotState

object CommandParser {

    private val commands = mapOf(
        "Show friends" to BotCommandHandler.LIST_FRIENDS,
        "Add friend" to BotCommandHandler.ADD_FRIEND,
        "Add attribute" to BotCommandHandler.ADD_GENERAL_ATTRIBUTE,
        "Delete attribute" to BotCommandHandler.REMOVE_GENERAL_ATTRIBUTE,
        "Help" to BotCommandHandler.HELP,
        "Reset profile" to BotCommandHandler.RESET
    )

    fun String.isCommand(): Boolean {
        return commands.containsKey(this)
                || enumContains<BotCommandHandler>(this.substring(1).uppercase())
                || this.isStorageCommand()
    }

    private fun String.isStorageCommand(): Boolean {
        if (this.length < 10) return false
        return this.endsWith("_storage") && enumContains<BotCommandHandler>(
            this.substring(1, this.length - 8).uppercase()
        )
    }

    fun String.toCommand(): BotCommandHandler {
        return try {
            if (commands.containsKey(this)) commands[this]!!
            else if (this.isStorageCommand()) enumValueOf(this.substring(1, this.length - 8).uppercase())
            else enumValueOf(this.substring(1).uppercase())
        } catch (e: IllegalArgumentException) {
            BotCommandHandler.UNKNOWN
        }
    }

    fun String.getState(): BotState {
        if (this.isStorageCommand()) return BotState.EXECUTE_USING_STORAGE
        return BotState.EXPECTING_COMMAND
    }

    private inline fun <reified T : Enum<T>> enumContains(name: String): Boolean {
        return enumValues<T>().any { it.name == name }
    }
}