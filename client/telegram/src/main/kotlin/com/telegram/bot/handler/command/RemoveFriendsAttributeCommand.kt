package com.telegram.bot.handler.command

import com.telegram.bot.dto.*
import com.telegram.bot.handler.BotState
import com.telegram.bot.handler.Buttons
import com.telegram.bot.service.FriendRequestService
import com.telegram.bot.utils.Commands
import com.telegram.bot.utils.CommandsMap
import com.telegram.bot.utils.Jedis.addToCommandsQueue
import com.telegram.bot.utils.Jedis.getValue
import com.telegram.bot.utils.Jedis.setValue
import com.telegram.bot.utils.RedisParams
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import javax.inject.Named

/**
 * Command that removes friend's existing attribute.
 */
@Component
@Named(Commands.REMOVE_FRIENDS_ATTRIBUTE)
class RemoveFriendsAttributeCommand(
    private val friendRequestService: FriendRequestService
) : Command {
    override fun description(): String {
        return "Removes friend's existing attribute"
    }

    override fun nextState(userId: Long): BotState {
        val botState = enumValueOf<BotState>(getValue(userId, RedisParams.STATE.name)!!)
        return when (botState) {
            BotState.EXPECTING_COMMAND -> BotState.EXPECTING_FRIEND_NAME
            BotState.EXPECTING_FRIEND_NAME, BotState.EXECUTE_USING_STORAGE -> BotState.EXPECTING_ATTRIBUTE_NAME
            BotState.EXPECTING_ATTRIBUTE_NAME -> BotState.EXPECTING_COMMAND
            else -> BotState.EXPECTING_COMMAND
        }
    }

    override fun getMessage(user: UserDTO, message: String): String {
        val botState = enumValueOf<BotState>(getValue(user.id, RedisParams.STATE.name)!!)
        return when (botState) {
            BotState.EXPECTING_COMMAND -> "Which friend would you like to remove attribute from?"
            BotState.EXPECTING_FRIEND_NAME -> {
                return try {
                    val friend = friendRequestService.getFriendByName(user.id, message)
                    setValue(user.id, RedisParams.FRIEND_ID.name, friend.id.toString())
                    "What attribute would you like to remove?"
                } catch (e: RuntimeException) {
                    addToCommandsQueue(user.id, Commands.LIST_FRIENDS)
                    setValue(user.id, RedisParams.STATE.name, BotState.ERROR.name)
                    e.message!!
                }
            }
            BotState.EXECUTE_USING_STORAGE -> "What attribute would you like to remove?"
            BotState.EXPECTING_ATTRIBUTE_NAME -> {
                return try {
                    addToCommandsQueue(user.id, Commands.FRIEND_INFO + Commands.STORAGE)
                    val friendId = getValue(user.id, RedisParams.FRIEND_ID.name)!!.toLong()
                    friendRequestService.deleteAttribute(user.id, friendId, message)
                } catch (e: RuntimeException) {
                    setValue(user.id, RedisParams.STATE.name, BotState.ERROR.name)
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