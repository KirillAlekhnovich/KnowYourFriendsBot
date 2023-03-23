package com.telegram.bot.handler.command

import com.telegram.bot.dto.UserDTO
import com.telegram.bot.handler.BotState
import com.telegram.bot.handler.Buttons
import com.telegram.bot.handler.Buttons.createInlineMarkup
import com.telegram.bot.service.UserRequestService
import com.telegram.bot.utils.Commands
import com.telegram.bot.utils.CommandsMap
import com.telegram.bot.utils.Jedis
import com.telegram.bot.utils.Jedis.addToCommandsQueue
import com.telegram.bot.utils.Jedis.reset
import com.telegram.bot.utils.Jedis.setValue
import com.telegram.bot.utils.RedisParams
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import java.util.ArrayList
import javax.inject.Named

@Component
@Named(Commands.RESET)
class ResetCommand(
    private val userRequestService: UserRequestService
) : Command {

    private val positiveResponses = listOf("Yes", "yes", "YES", "y", "Y")

    override fun description(): String {
        return "Resets your profile. Removes all your friends and general attributes"
    }

    override fun nextState(userId: Long): BotState {
        val botState = enumValueOf<BotState>(Jedis.getValue(userId, RedisParams.STATE.name)!!)
        return when (botState) {
            BotState.EXPECTING_COMMAND -> BotState.EXPECTING_APPROVAL
            BotState.EXPECTING_APPROVAL -> BotState.EXPECTING_COMMAND
            else -> BotState.EXPECTING_COMMAND
        }
    }

    override fun getMessage(user: UserDTO, message: String): String {
        val botState = enumValueOf<BotState>(Jedis.getValue(user.id, RedisParams.STATE.name)!!)
        return when (botState) {
            BotState.EXPECTING_COMMAND -> {
                "This action will delete all your friends and attributes. Are you sure?"
            }
            BotState.EXPECTING_APPROVAL -> {
                addToCommandsQueue(user.id, Commands.LIST_FRIENDS)
                if (!positiveResponses.contains(message)) return "Reset cancelled"
                try {
                    reset(user.id)
                    userRequestService.resetUser(user.id)
                } catch (e: RuntimeException) {
                    setValue(user.id, RedisParams.STATE.name, BotState.ERROR.name)
                    e.message!!
                }
            }
            else -> CommandsMap.get(Commands.UNKNOWN).getMessage(user, message)
        }
    }

    override fun getButtons(userId: Long): ReplyKeyboard? {
        val botState = enumValueOf<BotState>(Jedis.getValue(userId, RedisParams.STATE.name)!!)
        if (botState != BotState.EXPECTING_COMMAND) return null

        val buttons: MutableList<MutableList<InlineKeyboardButton>> = ArrayList()
        val row: MutableList<InlineKeyboardButton> = ArrayList()
        row.add(Buttons.createInlineButton("Yes"))
        row.add(Buttons.createInlineButton("No"))
        buttons.add(Buttons.createRowInstance(row))
        row.clear()
        return createInlineMarkup(buttons)
    }
}