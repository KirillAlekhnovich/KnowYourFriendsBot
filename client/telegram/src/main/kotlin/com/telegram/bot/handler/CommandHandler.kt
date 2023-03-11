package com.telegram.bot.handler

import com.telegram.bot.dto.ClientResponseDTO
import com.telegram.bot.dto.UserDTO
import com.telegram.bot.handler.command.Command
import com.telegram.bot.utils.Commands
import com.telegram.bot.utils.CommandsMap
import com.telegram.bot.utils.Jedis
import com.telegram.bot.utils.Jedis.addToCommandsQueue
import com.telegram.bot.utils.Jedis.getCommandsQueue
import com.telegram.bot.utils.Jedis.removeFirstCommandFromQueue
import com.telegram.bot.utils.RedisParams
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

    fun handle(user: UserDTO, message: String): List<ClientResponseDTO> {
        val responseMessages = mutableListOf<ClientResponseDTO>()
        val jedis = Jedis.get()
        jedis.addToCommandsQueue(user.id, message)
        while (jedis.getCommandsQueue(user.id).isNotEmpty()) {
            if (jedis.getCommandsQueue(user.id).first().isCommand()) {
                jedis.hset(user.id.toString(), RedisParams.STATE.name, jedis.getCommandsQueue(user.id).first().getState().name)
                jedis.hset(user.id.toString(), RedisParams.COMMAND.name, jedis.getCommandsQueue(user.id).first().toCommand())
            }
            val currCommand = CommandsMap.get(jedis.hget(user.id.toString(), RedisParams.COMMAND.name))
            responseMessages.add(currCommand.execute(user, message))
            jedis.removeFirstCommandFromQueue(user.id)
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