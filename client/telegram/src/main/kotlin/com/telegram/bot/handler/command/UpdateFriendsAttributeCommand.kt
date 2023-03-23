package com.telegram.bot.handler.command

import com.telegram.bot.dto.*
import com.telegram.bot.handler.BotState
import com.telegram.bot.handler.Buttons.createAttributesMarkup
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

@Component
@Named(Commands.UPDATE_FRIENDS_ATTRIBUTE)
class UpdateFriendsAttributeCommand(
    private val friendRequestService: FriendRequestService,
) : Command {
    override fun description(): String {
        return "Updates friend's existing attribute value"
    }

    override fun nextState(userId: Long): BotState {
        val botState = enumValueOf<BotState>(getValue(userId, RedisParams.STATE.name)!!)
        return when (botState) {
            BotState.EXPECTING_COMMAND -> BotState.EXPECTING_FRIEND_NAME
            BotState.EXPECTING_FRIEND_NAME, BotState.EXECUTE_USING_STORAGE -> BotState.EXPECTING_ATTRIBUTE_NAME
            BotState.EXPECTING_ATTRIBUTE_NAME -> BotState.EXPECTING_ATTRIBUTE_VALUE
            BotState.EXPECTING_ATTRIBUTE_VALUE -> BotState.EXPECTING_COMMAND
            else -> BotState.EXPECTING_COMMAND
        }
    }

    override fun getMessage(user: UserDTO, message: String): String {
        val botState = enumValueOf<BotState>(getValue(user.id, RedisParams.STATE.name)!!)
        return when (botState) {
            BotState.EXPECTING_COMMAND -> "Please specify friend's name"
            BotState.EXPECTING_FRIEND_NAME -> {
                try {
                    val friend = friendRequestService.getFriendByName(user.id, message)
                    setValue(user.id, RedisParams.FRIEND_ID.name, friend.id.toString())
                    "What attribute would you like to update?"
                } catch (e: RuntimeException) {
                    setValue(user.id, RedisParams.STATE.name, BotState.ERROR.name)
                    e.message!!
                }
            }
            BotState.EXECUTE_USING_STORAGE -> "What attribute would you like to update?"
            BotState.EXPECTING_ATTRIBUTE_NAME -> {
                try {
                    val friendId = getValue(user.id, RedisParams.FRIEND_ID.name)!!.toLong()
                    if (!friendRequestService.hasAttribute(user.id, friendId, message)) {
                        setValue(user.id, RedisParams.STATE.name, BotState.ERROR.name)
                        "Friend doesn't have attribute with name $message"
                    } else {
                        setValue(user.id, RedisParams.ATTRIBUTE_NAME.name, message)
                        "Please specify its new value"
                    }
                } catch (e: RuntimeException) {
                    setValue(user.id, RedisParams.STATE.name, BotState.ERROR.name)
                    e.message!!
                }
            }
            BotState.EXPECTING_ATTRIBUTE_VALUE -> {
                try {
                    addToCommandsQueue(user.id, Commands.FRIEND_INFO + Commands.STORAGE)
                    val friendId = getValue(user.id, RedisParams.FRIEND_ID.name)!!.toLong()
                    val attributeName = getValue(user.id, RedisParams.ATTRIBUTE_NAME.name)!!
                    friendRequestService.updateAttribute(
                        user.id,
                        friendId,
                        AttributeDTO(attributeName, message)
                    )
                } catch (e: RuntimeException) {
                    setValue(user.id, RedisParams.STATE.name, BotState.ERROR.name)
                    e.message!!
                }
            }
            else -> CommandsMap.get(Commands.UNKNOWN).getMessage(user, message)
        }
    }

    override fun getButtons(userId: Long): ReplyKeyboard? {
        return createAttributesMarkup(userId, friendRequestService)
    }
}