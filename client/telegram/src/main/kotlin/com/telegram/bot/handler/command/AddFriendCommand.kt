package com.telegram.bot.handler.command

import com.telegram.bot.dto.FriendDTO
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
@Named(Commands.ADD_FRIEND)
class AddFriendCommand(
    private val userRequestService: UserRequestService
) : Command {
    override fun description(): String {
        return "Adds new friend"
    }

    override fun nextState(userId: Long): BotState {
        val botState = enumValueOf<BotState>(Jedis.get().hget(userId.toString(), RedisParams.STATE.name))
        return when (botState) {
            BotState.EXPECTING_COMMAND -> BotState.EXPECTING_FRIEND_NAME
            BotState.EXPECTING_FRIEND_NAME -> BotState.EXPECTING_COMMAND
            else -> BotState.EXPECTING_COMMAND
        }
    }

    override fun getMessage(user: UserDTO, message: String): String {
        val jedis = Jedis.get()
        val botState = enumValueOf<BotState>(jedis.hget(user.id.toString(), RedisParams.STATE.name))
        return when (botState) {
            BotState.EXPECTING_COMMAND -> "Please enter the name of the friend"
            BotState.EXPECTING_FRIEND_NAME -> {
                jedis.addToCommandsQueue(user.id, Commands.LIST_FRIENDS)
                userRequestService.addFriend(user.id, FriendDTO(0, message, emptyMap<String, String?>().toMutableMap()))
            }
            else -> CommandsMap.get(Commands.UNKNOWN).getMessage(user, message)
        }
    }
}