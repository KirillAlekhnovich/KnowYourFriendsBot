package com.telegram.bot.handler

import com.telegram.bot.dto.TelegramBotStateDTO
import com.telegram.bot.dto.getParamFromStorage
import com.telegram.bot.service.FriendRequestService
import com.telegram.bot.utils.Commands
import com.telegram.bot.utils.StorageParams
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import java.util.ArrayList

object Buttons {
    fun createInlineButton(text: String, callback: String? = null): InlineKeyboardButton {
        val button = InlineKeyboardButton()
        button.text = text
        button.callbackData = callback ?: text
        return button
    }

    fun createKeyboardButton(text: String): KeyboardButton {
        val button = KeyboardButton()
        button.text = text
        return button
    }

    fun <T> createRowInstance(buttons: MutableList<T>): MutableList<T> {
        val row: MutableList<T> = ArrayList()
        row.addAll(buttons)
        return row
    }

    fun createInlineMarkup(buttons: List<MutableList<InlineKeyboardButton>>): ReplyKeyboard {
        val markup = InlineKeyboardMarkup()
        markup.keyboard = buttons
        return markup
    }

    fun createKeyboardMarkup(buttons: List<KeyboardRow>): ReplyKeyboard {
        val markup = ReplyKeyboardMarkup()
        markup.keyboard = buttons
        markup.resizeKeyboard = true
        return markup
    }

    fun createAttributesMarkup(
        telegramBotState: TelegramBotStateDTO,
        friendRequestService: FriendRequestService
    ): ReplyKeyboard? {
        if (telegramBotState.state != BotState.EXPECTING_FRIEND_NAME
            && telegramBotState.state != BotState.EXECUTE_USING_STORAGE
        ) return null
        return try {
            val friendId = telegramBotState.getParamFromStorage(StorageParams.FRIEND_ID).toLong()
            val friendAttributes = friendRequestService.getAttributeNames(friendId)
            val buttons: MutableList<MutableList<InlineKeyboardButton>> = ArrayList()
            val row: MutableList<InlineKeyboardButton> = ArrayList()
            val numberOfColumns = 2
            var split = 1
            friendAttributes.forEach {
                row.add(createInlineButton(it))
                if (split % numberOfColumns == 0) {
                    buttons.add(createRowInstance(row))
                    row.clear()
                }
                split++
            }
            row.add(createInlineButton("Back", Commands.FRIEND_INFO + Commands.STORAGE))
            if (row.isNotEmpty()) buttons.add(row)
            createInlineMarkup(buttons)
        } catch (e: Exception) {
            null
        }
    }
}

fun MutableList<KeyboardButton>.toKeyboardRow(): KeyboardRow {
    val row = KeyboardRow()
    row.addAll(this)
    return row
}