package com.telegram.bot.handler.command

import com.telegram.bot.dto.UserDTO
import com.telegram.bot.handler.BotState
import com.telegram.bot.service.FriendRequestService
import com.telegram.bot.service.UserRequestService
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
    private val userRequestService: UserRequestService,
    private val friendRequestService: FriendRequestService
): Command {
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
                val friend = userRequestService.getFriendByName(user.id, message)
                setValue(user.id, RedisParams.FRIEND_ID.name, friend.id.toString())
                "What would you like to change name to?"
            }
            BotState.EXECUTE_USING_STORAGE -> "What would you like to change name to?"
            BotState.EXPECTING_NEW_FRIEND_NAME -> {
                val friendId = getValue(user.id, RedisParams.FRIEND_ID.name)!!.toLong()
                addToCommandsQueue(user.id, Commands.FRIEND_INFO + Commands.STORAGE)
                friendRequestService.changeFriendsName(user.id, friendId, message)
            }
            else -> CommandsMap.get(Commands.UNKNOWN).getMessage(user, message)
        }
    }
}