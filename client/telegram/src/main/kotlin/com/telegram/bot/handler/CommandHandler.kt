package com.telegram.bot.handler

import com.telegram.bot.dto.ClientResponseDTO
import com.telegram.bot.dto.TelegramBotStateDTO
import com.telegram.bot.dto.UserDTO
import com.telegram.bot.handler.command.Command
import com.telegram.bot.utils.Commands
import com.telegram.bot.utils.CommandsMap
import org.springframework.stereotype.Component

@Component
class CommandHandler(private val commands: MutableMap<String, Command>) {

    init {
        val callbackCommands = mapOf(
            "Show friends" to commands[Commands.LIST_FRIENDS]!!,
            "Add friend" to commands[Commands.ADD_FRIEND]!!,
            "Add attribute" to commands[Commands.ADD_GENERAL_ATTRIBUTE]!!,
            "Remove attribute" to commands[Commands.REMOVE_GENERAL_ATTRIBUTE]!!,
            "Help" to commands[Commands.HELP]!!,
            "Reset profile" to commands[Commands.RESET]!!
        )

        commands.putAll(callbackCommands)
        CommandsMap.registerCommands(commands)
    }

    fun handle(user: UserDTO, message: String, botState: TelegramBotStateDTO): List<ClientResponseDTO> {
        val responseMessages = mutableListOf<ClientResponseDTO>()
        botState.commandsQueue.add(message)
        while (botState.commandsQueue.isNotEmpty()) {
            if (botState.commandsQueue.first().isCommand()) {
                if (botState.state != BotState.EXECUTE_USING_STORAGE) botState.state =
                    botState.commandsQueue.first().getState()
                botState.currCommand = botState.commandsQueue.first().toCommand()
            }
            responseMessages.add(CommandsMap.get(botState.currCommand).execute(user, message, botState))
            botState.commandsQueue.removeFirst()
        }
        return responseMessages
    }

    fun String.isCommand(): Boolean {
        return commands.containsKey(this) || this.isStorageCommand()
    }

    private fun String.isStorageCommand(): Boolean {
        if (this.length <= Commands.STORAGE.length) return false
        return this.endsWith(Commands.STORAGE) && commands[getStorageCommandPrefix(this)] != null
    }

    private fun getStorageCommandPrefix(storageCommand: String): String {
        return storageCommand.substring(0, storageCommand.length - Commands.STORAGE.length)
    }

    fun String.toCommand(): String {
        return if (this.isStorageCommand()) getStorageCommandPrefix(this) else this
    }

    fun String.getState(): BotState {
        if (this.isStorageCommand()) return BotState.EXECUTE_USING_STORAGE
        return BotState.EXPECTING_COMMAND
    }
}