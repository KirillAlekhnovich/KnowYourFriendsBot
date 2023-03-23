package com.telegram.bot.handler.command

import com.telegram.bot.dto.FriendDTO
import com.telegram.bot.dto.UserDTO
import com.telegram.bot.handler.BotState
import com.telegram.bot.service.FriendRequestService
import com.telegram.bot.utils.Commands
import com.telegram.bot.utils.CommandsMap
import com.telegram.bot.utils.Jedis.addToCommandsQueue
import com.telegram.bot.utils.Jedis.getValue
import com.telegram.bot.utils.Jedis.setValue
import com.telegram.bot.utils.RedisParams
import org.springframework.stereotype.Component
import javax.inject.Named

@Component
@Named(Commands.ADD_FRIEND)
class AddFriendCommand(
    private val friendRequestService: FriendRequestService
) : Command {
    override fun description(): String {
        return "Adds new friend"
    }

    override fun nextState(userId: Long): BotState {
        val botState = enumValueOf<BotState>(getValue(userId, RedisParams.STATE.name)!!)
        return when (botState) {
            BotState.EXPECTING_COMMAND -> BotState.EXPECTING_FRIEND_NAME
            BotState.EXPECTING_FRIEND_NAME -> BotState.EXPECTING_COMMAND
            else -> BotState.EXPECTING_COMMAND
        }
    }

    override fun getMessage(user: UserDTO, message: String): String {
        val botState = enumValueOf<BotState>(getValue(user.id, RedisParams.STATE.name)!!)
        return when (botState) {
            BotState.EXPECTING_COMMAND -> "Please enter the name of the friend"
            BotState.EXPECTING_FRIEND_NAME -> {
                try {
                    addToCommandsQueue(user.id, Commands.LIST_FRIENDS)
                    friendRequestService.addFriend(
                        user.id,
                        FriendDTO(0, message, user.id, emptyMap<String, String?>().toMutableMap())
                    )
                } catch (e: RuntimeException) {
                    setValue(user.id, RedisParams.STATE.name, BotState.ERROR.name)
                    e.message!!
                }
            }
            else -> CommandsMap.get(Commands.UNKNOWN).getMessage(user, message)
        }
    }
}