package com.telegram.bot.handler.command

import com.telegram.bot.dto.*
import com.telegram.bot.handler.BotState
import com.telegram.bot.handler.Buttons
import com.telegram.bot.service.FriendRequestService
import com.telegram.bot.service.UserRequestService
import com.telegram.bot.utils.Commands
import com.telegram.bot.utils.CommandsMap
import com.telegram.bot.utils.Jedis
import com.telegram.bot.utils.Jedis.addToCommandsQueue
import com.telegram.bot.utils.RedisParams
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import javax.inject.Named

@Component
@Named(Commands.REMOVE_FRIENDS_ATTRIBUTE)
class RemoveFriendsAttributeCommand(
    private val friendRequestService: FriendRequestService,
    private val userRequestService: UserRequestService
) : Command {
    override fun description(): String {
        return "Removes friend's existing attribute"
    }

    override fun nextState(userId: Long): BotState {
        val botState = Jedis.get().hget(userId.toString(), RedisParams.STATE.name)
        return when (botState) {
            BotState.EXPECTING_COMMAND.name -> BotState.EXPECTING_FRIEND_NAME
            BotState.EXPECTING_FRIEND_NAME.name, BotState.EXECUTE_USING_STORAGE.name -> BotState.EXPECTING_ATTRIBUTE_NAME
            BotState.EXPECTING_ATTRIBUTE_NAME.name -> BotState.EXPECTING_COMMAND
            else -> BotState.EXPECTING_COMMAND
        }
    }

    override fun getMessage(user: UserDTO, message: String): String {
        val jedis = Jedis.get()
        val botState = jedis.hget(user.id.toString(), RedisParams.STATE.name)
        return when (botState) {
            BotState.EXPECTING_COMMAND.name -> "Which friend would you like to remove attribute from?"
            BotState.EXPECTING_FRIEND_NAME.name -> {
                return try {
                    val friend = userRequestService.getFriendByName(user.id, message)
                    jedis.hset(user.id.toString(), RedisParams.FRIEND_ID.name, friend.id.toString())
                    "What attribute would you like to remove?"
                } catch (e: RuntimeException) {
                    jedis.hset(user.id.toString(), RedisParams.STATE.name, BotState.ERROR.name)
                    e.message!!
                }
            }
            BotState.EXECUTE_USING_STORAGE.name -> "What attribute would you like to remove?"
            BotState.EXPECTING_ATTRIBUTE_NAME.name -> {
                return try {
                    jedis.addToCommandsQueue(user.id, Commands.FRIEND_INFO + Commands.STORAGE)
                    val friendId = jedis.hget(user.id.toString(), RedisParams.FRIEND_ID.name).toLong()
                    friendRequestService.deleteAttribute(friendId, message)
                } catch (e: RuntimeException) {
                    jedis.hset(user.id.toString(), RedisParams.STATE.name, BotState.ERROR.name)
                    e.message!!
                }
            }
            else -> CommandsMap.get(Commands.UNKNOWN).getMessage(user, message)
        }
    }

    override fun getButtons(userId: Long): ReplyKeyboard? {
        return Buttons.createAttributesMarkup(userId, friendRequestService)
    }
}