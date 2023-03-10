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
import com.telegram.bot.utils.StorageParams
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

    override fun nextState(botState: TelegramBotStateDTO): BotState {
        return when (botState.state) {
            BotState.EXPECTING_COMMAND -> BotState.EXPECTING_FRIEND_NAME
            BotState.EXPECTING_FRIEND_NAME -> BotState.EXPECTING_COMMAND
            BotState.EXECUTE_USING_STORAGE -> BotState.EXPECTING_COMMAND
            else -> BotState.EXPECTING_COMMAND
        }
    }

    override fun getMessage(user: UserDTO, message: String, telegramBotState: TelegramBotStateDTO): String {
        return when (telegramBotState.state) {
            BotState.EXPECTING_COMMAND -> "Please enter the name of the friend"
            BotState.EXPECTING_FRIEND_NAME -> {
                try {
                    val friend = userRequestService.getFriendByName(user.id, message)
                    telegramBotState.addParamToStorage(StorageParams.FRIEND_ID, friend.id.toString())
                    printFriendInfo(friend.id)
                } catch (e: RuntimeException) {
                    telegramBotState.commandsQueue.add(Commands.LIST_FRIENDS + Commands.STORAGE)
                    e.message!!
                }
            }
            BotState.EXECUTE_USING_STORAGE -> {
                try {
                    val friendId = telegramBotState.getParamFromStorage(StorageParams.FRIEND_ID).toLong()
                    printFriendInfo(friendId)
                } catch (e: RuntimeException) {
                    telegramBotState.commandsQueue.add(Commands.LIST_FRIENDS + Commands.STORAGE)
                    e.message!!
                }
            }
            else -> CommandsMap.get(Commands.UNKNOWN).getMessage(user, message, telegramBotState)
        }
    }

    override fun getButtons(botState: TelegramBotStateDTO): ReplyKeyboard? {
        if (!botState.paramIsInStorage(StorageParams.FRIEND_ID)
            || botState.state == BotState.EXPECTING_COMMAND
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