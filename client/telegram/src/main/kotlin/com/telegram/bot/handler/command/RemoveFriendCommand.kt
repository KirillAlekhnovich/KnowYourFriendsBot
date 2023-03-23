package com.telegram.bot.handler.command

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
@Named(Commands.REMOVE_FRIEND)
class RemoveFriendCommand(
    private val friendRequestService: FriendRequestService
) : Command {
    override fun description(): String {
        return "Removes existing friend"
    }

    override fun nextState(userId: Long): BotState {
        val botState = enumValueOf<BotState>(getValue(userId, RedisParams.STATE.name)!!)
        return when (botState) {
            BotState.EXPECTING_COMMAND -> BotState.EXPECTING_FRIEND_NAME
            BotState.EXPECTING_FRIEND_NAME, BotState.EXECUTE_USING_STORAGE -> BotState.EXPECTING_COMMAND
            else -> BotState.EXPECTING_COMMAND
        }
    }

    override fun getMessage(user: UserDTO, message: String): String {
        val botState = enumValueOf<BotState>(getValue(user.id, RedisParams.STATE.name)!!)
        return when (botState) {
            BotState.EXPECTING_COMMAND -> "Which friend would you like to remove?"
            BotState.EXPECTING_FRIEND_NAME -> {
                try {
                    addToCommandsQueue(user.id, Commands.LIST_FRIENDS)
                    val friendId = friendRequestService.getFriendByName(user.id, message).id
                    friendRequestService.removeFriend(user.id, friendId)
                } catch (e: RuntimeException) {
                    setValue(user.id, RedisParams.STATE.name, BotState.ERROR.name)
                    e.message!!
                }
            }
            BotState.EXECUTE_USING_STORAGE -> {
                try {
                    addToCommandsQueue(user.id, Commands.LIST_FRIENDS)
                    val friendId = getValue(user.id, RedisParams.FRIEND_ID.name)!!.toLong()
                    friendRequestService.removeFriend(user.id, friendId)
                } catch (e: RuntimeException) {
                    setValue(user.id, RedisParams.STATE.name, BotState.ERROR.name)
                    e.message!!
                }
            }
            else -> CommandsMap.get(Commands.UNKNOWN).getMessage(user, message)
        }
    }
}