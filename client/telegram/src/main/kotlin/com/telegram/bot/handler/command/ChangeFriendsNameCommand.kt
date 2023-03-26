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
@Named(Commands.CHANGE_FRIENDS_NAME)
class ChangeFriendsNameCommand(
    private val friendRequestService: FriendRequestService
) : Command {
    override fun description(): String {
        return "Changes friends name"
    }

    override fun nextState(userId: Long): BotState {
        val botState = enumValueOf<BotState>(getValue(userId, RedisParams.STATE.name)!!)
        return when (botState) {
            BotState.EXPECTING_COMMAND -> BotState.EXPECTING_FRIEND_NAME
            BotState.EXPECTING_FRIEND_NAME, BotState.EXECUTE_USING_STORAGE -> BotState.EXPECTING_NEW_FRIEND_NAME
            else -> BotState.EXPECTING_COMMAND
        }
    }

    override fun getMessage(user: UserDTO, message: String): String {
        val botState = enumValueOf<BotState>(getValue(user.id, RedisParams.STATE.name)!!)
        return when (botState) {
            BotState.EXPECTING_COMMAND -> "Which friend's name would you like to change?"
            BotState.EXPECTING_FRIEND_NAME -> {
                try {
                    val friend = friendRequestService.getFriendByName(user.id, message)
                    setValue(user.id, RedisParams.FRIEND_ID.name, friend.id.toString())
                    "What would you like to change name to?"
                } catch (e: RuntimeException) {
                    addToCommandsQueue(user.id, Commands.LIST_FRIENDS)
                    setValue(user.id, RedisParams.STATE.name, BotState.ERROR.name)
                    e.message!!
                }
            }
            BotState.EXECUTE_USING_STORAGE -> "What would you like to change name to?"
            BotState.EXPECTING_NEW_FRIEND_NAME -> {
                try {
                    addToCommandsQueue(user.id, Commands.FRIEND_INFO + Commands.STORAGE)
                    val friendId = getValue(user.id, RedisParams.FRIEND_ID.name)!!.toLong()
                    friendRequestService.changeFriendsName(user.id, friendId, message)
                } catch (e: RuntimeException) {
                    setValue(user.id, RedisParams.STATE.name, BotState.ERROR.name)
                    e.message!!
                }
            }
            else -> CommandsMap.get(Commands.UNKNOWN).getMessage(user, message)
        }
    }
}