package com.telegram.bot.handler.command

import com.telegram.bot.dto.*
import com.telegram.bot.handler.BotState
import com.telegram.bot.service.FriendRequestService
import com.telegram.bot.service.UserRequestService
import com.telegram.bot.utils.Commands
import com.telegram.bot.utils.CommandsMap
import com.telegram.bot.utils.Jedis
import com.telegram.bot.utils.Jedis.addToCommandsQueue
import com.telegram.bot.utils.RedisParams
import org.springframework.stereotype.Component
import javax.inject.Named

@Component
@Named(Commands.ADD_FRIENDS_ATTRIBUTE)
class AddFriendsAttributeCommand(
    private val friendRequestService: FriendRequestService,
    private val userRequestService: UserRequestService
) : Command {
    override fun description(): String {
        return "Adds new attribute to a friend"
    }

    override fun nextState(userId: Long): BotState {
        val botState = enumValueOf<BotState>(Jedis.get().hget(userId.toString(), RedisParams.STATE.name))
        return when (botState) {
            BotState.EXPECTING_COMMAND -> BotState.EXPECTING_FRIEND_NAME
            BotState.EXPECTING_FRIEND_NAME, BotState.EXECUTE_USING_STORAGE -> BotState.EXPECTING_ATTRIBUTE_NAME
            BotState.EXPECTING_ATTRIBUTE_NAME -> BotState.EXPECTING_ATTRIBUTE_VALUE
            BotState.EXPECTING_ATTRIBUTE_VALUE -> BotState.EXPECTING_COMMAND
            else -> BotState.EXPECTING_COMMAND
        }
    }

    override fun getMessage(user: UserDTO, message: String): String {
        val jedis = Jedis.get()
        val botState = enumValueOf<BotState>(jedis.hget(user.id.toString(), RedisParams.STATE.name))
        return when (botState) {
            BotState.EXPECTING_COMMAND -> "Please specify friend's name"
            BotState.EXPECTING_FRIEND_NAME -> {
                try {
                    val friend = userRequestService.getFriendByName(user.id, message)
                    jedis.hset(user.id.toString(), RedisParams.FRIEND_ID.name, friend.id.toString())
                    "What attribute would you like to add?"
                } catch (e: RuntimeException) {
                    jedis.hset(user.id.toString(), RedisParams.STATE.name, BotState.ERROR.name)
                    e.message!!
                }
            }
            BotState.EXECUTE_USING_STORAGE -> "What attribute would you like to add?"
            BotState.EXPECTING_ATTRIBUTE_NAME -> {
                try {
                    val friendId = jedis.hget(user.id.toString(), RedisParams.FRIEND_ID.name).toLong()
                    if (friendRequestService.hasAttribute(friendId, message)) {
                        jedis.hset(user.id.toString(), RedisParams.STATE.name, BotState.ERROR.name)
                        return "Friend already has attribute with name $message"
                    }
                    jedis.hset(user.id.toString(), RedisParams.ATTRIBUTE_NAME.name, message)
                    "Please specify its value"
                } catch (e: RuntimeException) {
                    jedis.hset(user.id.toString(), RedisParams.STATE.name, BotState.ERROR.name)
                    e.message!!
                }
            }
            BotState.EXPECTING_ATTRIBUTE_VALUE -> {
                try {
                    jedis.addToCommandsQueue(user.id, Commands.FRIEND_INFO + Commands.STORAGE)
                    val friendId = jedis.hget(user.id.toString(), RedisParams.FRIEND_ID.name).toLong()
                    val attributeName = jedis.hget(user.id.toString(), RedisParams.ATTRIBUTE_NAME.name)
                    friendRequestService.addAttribute(friendId, AttributeDTO(attributeName, message))
                } catch (e: RuntimeException) {
                    jedis.hset(user.id.toString(), RedisParams.STATE.name, BotState.ERROR.name)
                    e.message!!
                }
            }
            else -> CommandsMap.get(Commands.UNKNOWN).getMessage(user, message)
        }
    }
}