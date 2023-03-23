package com.telegram.bot.handler

import com.telegram.bot.service.FriendRequestService
import com.telegram.bot.utils.Commands
import com.telegram.bot.utils.Jedis.getValue
import com.telegram.bot.utils.RedisParams
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

    fun createAttributesMarkup(userId: Long, friendRequestService: FriendRequestService): ReplyKeyboard? {
        val botState = enumValueOf<BotState>(getValue(userId, RedisParams.STATE.name)!!)
        if (botState != BotState.EXPECTING_FRIEND_NAME && botState != BotState.EXECUTE_USING_STORAGE) return null
        return try {
            val friendId = getValue(userId, RedisParams.FRIEND_ID.name)!!.toLong()
            val friendAttributes = friendRequestService.getAttributeNames(userId, friendId)
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