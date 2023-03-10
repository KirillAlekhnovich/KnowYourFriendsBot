package com.telegram.bot.handler.command

import com.telegram.bot.dto.TelegramBotStateDTO
import com.telegram.bot.dto.UserDTO
import com.telegram.bot.handler.BotState
import com.telegram.bot.handler.Buttons.createKeyboardButton
import com.telegram.bot.handler.Buttons.createKeyboardMarkup
import com.telegram.bot.handler.Buttons.createRowInstance
import com.telegram.bot.handler.toKeyboardRow
import com.telegram.bot.utils.Commands
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import java.util.ArrayList
import javax.inject.Named

@Component
@Named(Commands.START)
class StartCommand : Command {

    override fun description(): String {
        return "Starts conversation with bot"
    }

    override fun nextState(botState: TelegramBotStateDTO): BotState {
        return BotState.EXPECTING_COMMAND
    }

    override fun getMessage(
        user: UserDTO,
        message: String,
        telegramBotState: TelegramBotStateDTO
    ): String {
        return "Hello! I'm Know Your Friends Bot. My goal is to keep detailed info about your friends for you. " +
                "Please, select one of the options below to continue"
    }

    override fun getButtons(botState: TelegramBotStateDTO): ReplyKeyboard? {
        val buttons: MutableList<KeyboardRow> = ArrayList()
        val row = KeyboardRow()

        row.add(createKeyboardButton("Show friends"))
        row.add(createKeyboardButton("Add attribute"))
        row.add(createKeyboardButton("Help"))

        buttons.add(createRowInstance(row).toKeyboardRow())
        row.clear()

        row.add(createKeyboardButton("Add friend"))
        row.add(createKeyboardButton("Remove attribute"))
        row.add(createKeyboardButton("Reset profile"))

        buttons.add(createRowInstance(row).toKeyboardRow())

        return createKeyboardMarkup(buttons)
    }
}