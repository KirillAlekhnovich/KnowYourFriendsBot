package com.telegram.bot.handler.command

import com.telegram.bot.dto.UserDTO
import com.telegram.bot.handler.BotState
import com.telegram.bot.service.UserRequestService
import com.telegram.bot.utils.Commands
import com.telegram.bot.utils.CommandsMap
import com.telegram.bot.utils.Jedis
import com.telegram.bot.utils.Jedis.addToCommandsQueue
import com.telegram.bot.utils.Jedis.getValue
import com.telegram.bot.utils.RedisParams
import org.springframework.stereotype.Component
import javax.inject.Named

@Component
@Named(Commands.ADD_GENERAL_ATTRIBUTE)
class AddGeneralAttributeCommand(
    private val userRequestService: UserRequestService
) : Command {
    override fun description(): String {
        return "Adds new general attribute (will be added to all friends)"
    }

    override fun nextState(userId: Long): BotState {
        val botState = enumValueOf<BotState>(getValue(userId, RedisParams.STATE.name)!!)
        return when (botState) {
            BotState.EXPECTING_COMMAND -> BotState.EXPECTING_ATTRIBUTE_NAME
            BotState.EXPECTING_ATTRIBUTE_NAME -> BotState.EXPECTING_COMMAND
            else -> BotState.EXPECTING_COMMAND
        }
    }

    override fun getMessage(user: UserDTO, message: String): String {
        val botState = enumValueOf<BotState>(getValue(user.id, RedisParams.STATE.name)!!)
        return when (botState) {
            BotState.EXPECTING_COMMAND -> "What attribute would you like to add?"
            BotState.EXPECTING_ATTRIBUTE_NAME -> {
                addToCommandsQueue(user.id, Commands.LIST_FRIENDS)
                userRequestService.addGeneralAttribute(user.id, message)
            }
            else -> CommandsMap.get(Commands.UNKNOWN).getMessage(user, message)
        }
    }
}