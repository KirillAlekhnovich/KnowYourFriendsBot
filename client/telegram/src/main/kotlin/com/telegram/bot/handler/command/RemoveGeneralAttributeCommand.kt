package com.telegram.bot.handler.command

import com.telegram.bot.dto.UserDTO
import com.telegram.bot.handler.BotState
import com.telegram.bot.service.UserRequestService
import com.telegram.bot.utils.Commands
import com.telegram.bot.utils.CommandsMap
import com.telegram.bot.utils.Jedis
import com.telegram.bot.utils.Jedis.addToCommandsQueue
import com.telegram.bot.utils.RedisParams
import org.springframework.stereotype.Component
import javax.inject.Named

@Component
@Named(Commands.REMOVE_GENERAL_ATTRIBUTE)
class RemoveGeneralAttributeCommand(
    private val userRequestService: UserRequestService
) : Command {
    override fun description(): String {
        return "Removes existing general attribute (will be removed from all of your friends)"
    }

    override fun nextState(userId: Long): BotState {
        val botState = Jedis.get().hget(userId.toString(), RedisParams.STATE.name)
        return when (botState) {
            BotState.EXPECTING_COMMAND.name -> BotState.EXPECTING_ATTRIBUTE_NAME
            BotState.EXPECTING_ATTRIBUTE_NAME.name -> BotState.EXPECTING_COMMAND
            else -> BotState.EXPECTING_COMMAND
        }
    }

    override fun getMessage(user: UserDTO, message: String): String {
        val jedis = Jedis.get()
        val botState = jedis.hget(user.id.toString(), RedisParams.STATE.name)
        return when (botState) {
            BotState.EXPECTING_COMMAND.name -> "What attribute would you like to remove?"
            BotState.EXPECTING_ATTRIBUTE_NAME.name -> {
                jedis.addToCommandsQueue(user.id, Commands.LIST_FRIENDS)
                userRequestService.removeGeneralAttribute(user.id, message)
            }
            else -> CommandsMap.get(Commands.UNKNOWN).getMessage(user, message)
        }
    }
}