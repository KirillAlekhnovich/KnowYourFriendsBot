package com.telegram.bot.handler.command

import com.telegram.bot.dto.*
import com.telegram.bot.handler.BotState
import com.telegram.bot.handler.Buttons.createInlineButton
import com.telegram.bot.handler.Buttons.createInlineMarkup
import com.telegram.bot.handler.Buttons.createRowInstance
import com.telegram.bot.service.FriendRequestService
import com.telegram.bot.utils.Commands
import com.telegram.bot.utils.CommandsMap
import com.telegram.bot.utils.Jedis.addToCommandsQueue
import com.telegram.bot.utils.Jedis.exists
import com.telegram.bot.utils.Jedis.getValue
import com.telegram.bot.utils.Jedis.setValue
import com.telegram.bot.utils.RedisParams
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import java.util.ArrayList
import javax.inject.Named

/**
 * Command that shows info about specific friend.
 */
@Component
@Named(Commands.FRIEND_INFO)
class FriendInfoCommand(
    private val friendRequestService: FriendRequestService
) : Command {
    override fun description(): String {
        return "Shows info about specific friend"
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
            BotState.EXPECTING_COMMAND -> "Please enter the name of the friend"
            BotState.EXPECTING_FRIEND_NAME -> {
                try {
                    val friend = friendRequestService.getFriendByName(user.id, message)
                    setValue(user.id, RedisParams.FRIEND_ID.name, friend.id.toString())
                    printFriendInfo(user.id, friend.id)
                } catch (e: RuntimeException) {
                    addToCommandsQueue(user.id, Commands.LIST_FRIENDS + Commands.STORAGE)
                    e.message!!
                }
            }
            BotState.EXECUTE_USING_STORAGE -> {
                try {
                    val friendId = getValue(user.id, RedisParams.FRIEND_ID.name)!!.toLong()
                    printFriendInfo(user.id, friendId)
                } catch (e: RuntimeException) {
                    addToCommandsQueue(user.id, Commands.LIST_FRIENDS + Commands.STORAGE)
                    e.message!!
                }
            }
            else -> CommandsMap.get(Commands.UNKNOWN).getMessage(user, message)
        }
    }

    override fun getButtons(userId: Long): ReplyKeyboard? {
        val botState = enumValueOf<BotState>(getValue(userId, RedisParams.STATE.name)!!)

        if (!exists(userId, RedisParams.FRIEND_ID.name) || botState == BotState.EXPECTING_COMMAND) return null
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

        row.add(createInlineButton("Change name", Commands.CHANGE_FRIENDS_NAME + Commands.STORAGE))
        row.add(createInlineButton("Back to list", Commands.LIST_FRIENDS))
        buttons.add(createRowInstance(row))

        return createInlineMarkup(buttons)
    }

    private fun printFriendInfo(userId: Long, friendId: Long): String {
        val friend = friendRequestService.getFriend(userId, friendId)
        val attributes = friendRequestService.getAttributes(userId, friendId)
        val stringBuilder = StringBuilder()
        attributes.forEach { stringBuilder.append("${it.name}: ${it.value}\n") }
        if (stringBuilder.isEmpty()) stringBuilder.append("Friend has no attributes")
        return "Info about ${friend.name}:\n\n${stringBuilder}"
    }
}