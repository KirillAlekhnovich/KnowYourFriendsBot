package com.telegram.bot.utils

import com.telegram.bot.handler.command.Command
import org.springframework.stereotype.Component

@Component
object CommandsMap {
    private val commandsMap = mutableMapOf<String, Command>()

    fun registerCommands(commands: Map<String, Command>) {
        commandsMap.putAll(commands)
    }

    fun getAllCommands(): Map<String, Command> {
        return commandsMap
    }

    fun getDefaultCommands(): Map<String, Command> {
        return commandsMap.filter { it.key.startsWith('/') }
    }

    fun getCallbackCommands(): Map<String, Command> {
        return commandsMap.filter { !it.key.startsWith('/') }
    }

    fun get(key: String): Command {
        return commandsMap[key] ?: throw IllegalArgumentException("Command $key was not found")
    }
}