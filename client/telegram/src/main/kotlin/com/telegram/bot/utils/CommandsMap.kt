package com.telegram.bot.utils

import com.telegram.bot.handler.command.Command
import org.springframework.stereotype.Component

/**
 * Object that stores all commands aliases and their related commands.
 */
@Component
object CommandsMap {
    private val commandsMap = mutableMapOf<String, Command>()

    /**
     * Adding new commands to the map of commands.
     */
    fun registerCommands(commands: Map<String, Command>) {
        commandsMap.putAll(commands)
    }

    /**
     * Returns commands map.
     */
    fun getAllCommands(): Map<String, Command> {
        return commandsMap
    }

    /**
     * Returns default commands map.
     */
    fun getDefaultCommands(): Map<String, Command> {
        return commandsMap.filter { it.key.startsWith('/') }
    }

    /**
     * Returns callback commands map.
     */
    fun getCallbackCommands(): Map<String, Command> {
        return commandsMap.filter { !it.key.startsWith('/') }
    }

    /**
     * Returns command by its alias.
     */
    fun get(key: String): Command {
        return commandsMap[key] ?: throw IllegalArgumentException("Command $key was not found")
    }
}