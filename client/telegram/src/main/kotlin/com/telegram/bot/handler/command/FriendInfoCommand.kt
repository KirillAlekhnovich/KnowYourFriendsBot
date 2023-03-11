package com.telegram.bot.handler.command

import com.telegram.bot.dto.*
import com.telegram.bot.handler.BotState
import com.telegram.bot.handler.Buttons.createInlineButton
import com.telegram.bot.handler.Buttons.createInlineMarkup
import com.telegram.bot.handler.Buttons.createRowInstance
import com.telegram.bot.service.FriendRequestService
import com.telegram.bot.service.UserRequestService
import com.telegram.bot.utils.Commands
import com.telegram.bot.utils.CommandsMap
import com.telegram.bot.utils.Jedis
import com.telegram.bot.utils.Jedis.addToCommandsQueue
import com.telegram.bot.utils.RedisParams
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import java.util.ArrayList
import javax.inject.Named

@Component
@Named(Commands.FRIEND_INFO)
class FriendInfoCommand(
    private val friendRequestService: FriendRequestService,
    private val userRequestService: UserRequestService
) : Command {
    override fun description(): String {
        return "Shows info about specific friend"
    }

    override fun nextState(userId: Long): BotState {
        val botState = Jedis.get().hget(userId.toString(), RedisParams.STATE.name)
        return when (botState) {
            BotState.EXPECTING_COMMAND.name -> BotState.EXPECTING_FRIEND_NAME
            BotState.EXPECTING_FRIEND_NAME.name -> BotState.EXPECTING_COMMAND
            BotState.EXECUTE_USING_STORAGE.name -> BotState.EXPECTING_COMMAND
            else -> BotState.EXPECTING_COMMAND
        }
    }

    override fun getMessage(user: UserDTO, message: String): String {
        val jedis = Jedis.get()
        val botState = jedis.hget(user.id.toString(), RedisParams.STATE.name)
        return when (botState) {
            BotState.EXPECTING_COMMAND.name -> "Please enter the name of the friend"
            BotState.EXPECTING_FRIEND_NAME.name -> {
                try {
                    val friend = userRequestService.getFriendByName(user.id, message)
                    jedis.hset(user.id.toString(), RedisParams.FRIEND_ID.name, friend.id.toString())
                    printFriendInfo(friend.id)
                } catch (e: RuntimeException) {
                    jedis.addToCommandsQueue(user.id, Commands.LIST_FRIENDS + Commands.STORAGE)
                    e.message!!
                }
            }
            BotState.EXECUTE_USING_STORAGE.name -> {
                try {
                    val friendId = jedis.hget(user.id.toString(), RedisParams.FRIEND_ID.name).toLong()
                    printFriendInfo(friendId)
                } catch (e: RuntimeException) {
                    jedis.addToCommandsQueue(user.id, Commands.LIST_FRIENDS + Commands.STORAGE)
                    e.message!!
                }
            }
            else -> CommandsMap.get(Commands.UNKNOWN).getMessage(user, message)
        }
    }

    override fun getButtons(userId: Long): ReplyKeyboard? {
        val jedis = Jedis.get()
        val botState = jedis.hget(userId.toString(), RedisParams.STATE.name)
        if (!jedis.hexists(userId.toString(), RedisParams.FRIEND_ID.name)
            || botState == BotState.EXPECTING_COMMAND.name
        ) return null
        val buttons: MutableList<MutableList<InlineKeyboardButton>> = ArrayList()
        val row: MutableList<InlineKeyboardButton> = ArrayList()

        row.add(createInlineButton("Add attribute", Commands.ADD_FRIENDS_ATTRIBUTE + Commands.STORAGE))
        row.add(createInlineButton("Update attribute", Commands.UPDATE_FRIENDS_ATTRIBUTE + Commands.STORAGE))
        buttons.add(createRowInstance(row))
        row.clear()

        row.add(createInlineButton("Remove attribute", Commands.REMOVE_FRIENDS_ATTRIBUTE + Commands.STORAGE))
        row.add(createInlineButton("Remove friend", Commands.REMOVE_FRIEND + Commands.STORAGE))
        buttons.add(createRowInstance(row))
        row.clear()

        row.add(createInlineButton("Back to list", Commands.LIST_FRIENDS))
        buttons.add(createRowInstance(row))

        return createInlineMarkup(buttons)
    }

    private fun printFriendInfo(friendId: Long): String {
        val friend = friendRequestService.getFriend(friendId)
        val attributes = friendRequestService.getAttributes(friendId)
        val stringBuilder = StringBuilder()
        for ((name, value) in attributes) {
            stringBuilder.append("$name: $value\n")
        }
        if (stringBuilder.isEmpty()) stringBuilder.append("Friend has no attributes")
        return "Info about ${friend.name}:\n\n${stringBuilder}"
    }
}