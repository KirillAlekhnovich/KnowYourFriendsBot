package com.telegram.bot.utils

import com.telegram.bot.dto.UserDTO
import com.telegram.bot.handler.BotState
import com.telegram.bot.handler.CommandHandler
import com.telegram.bot.service.UserRequestService
import com.telegram.bot.utils.Jedis.exists
import com.telegram.bot.utils.Jedis.getValue
import com.telegram.bot.utils.Jedis.setValue
import org.springframework.stereotype.Service
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup

/**
 * Class that represents Telegram bot configuration.
 */
@Service
class TelegramBotConfig(
    private val userRequestService: UserRequestService,
    private val commandHandler: CommandHandler
) : TelegramLongPollingBot() {

    /**
     * Gets bot token from environmental variable.
     */
    override fun getBotToken(): String = System.getenv("BotTgToken")

    /**
     * Gets bot username from environmental variable.
     */
    override fun getBotUsername(): String = System.getenv("BotName")

    /**
     * Handling received update.
     */
    override fun onUpdateReceived(update: Update) {
        val (chatId, message, messageId) = getMessageData(update)
        val user = getUser(chatId)
        redisInitialSetup(chatId, messageId)
        sendResponse(user, message)
        switchState(chatId)
    }

    /**
     * Gets chat id, message id and message itself.
     */
    private fun getMessageData(update: Update): Triple<Long, String, String> {
        val chatId: Long
        val message: String
        val messageId: String
        if (update.hasCallbackQuery()) {
            chatId = update.callbackQuery.from.id
            message = update.callbackQuery.data
            messageId = update.callbackQuery.message.messageId.toString()
        } else if (update.hasMessage()) {
            chatId = update.message.chatId
            message = update.message.text
            messageId = update.message.messageId.toString()
        } else throw RuntimeException("No message received")
        return Triple(chatId, message, messageId)
    }

    /**
     * Gets user from database or creates new one.
     */
    private fun getUser(chatId: Long): UserDTO {
        if (!userRequestService.exists(chatId)) {
            setValue(chatId, RedisParams.ACCESS_TOKEN.name, userRequestService.registerUser(chatId))
        }
        return userRequestService.getUser(chatId)
    }

    /**
     * Initializes Redis database. Sets default state and command.
     */
    private fun redisInitialSetup(chatId: Long, messageId: String) {
        if (!exists(chatId, RedisParams.COMMAND.name)) {
            setValue(chatId, RedisParams.COMMAND.name, Commands.START)
        }
        if (!exists(chatId, RedisParams.STATE.name)) {
            setValue(chatId, RedisParams.STATE.name, BotState.EXPECTING_COMMAND.name)
        }
        setValue(chatId, RedisParams.CALLBACK_MESSAGE_ID.name, messageId)
    }

    /**
     * Sends response to user.
     */
    private fun sendResponse(user: UserDTO, message: String) {
        val response = commandHandler.handle(user, message)
        response.forEach {
            when (it.type) {
                MessageType.SEND -> {
                    val responseMsg = SendMessage(user.id.toString(), it.message)
                    responseMsg.replyMarkup = it.markup
                    execute(responseMsg)
                }
                MessageType.EDIT -> {
                    val editMessage = EditMessageText()
                    editMessage.chatId = user.id.toString()
                    editMessage.messageId = getValue(user.id, RedisParams.CALLBACK_MESSAGE_ID.name)!!.toInt()
                    editMessage.text = it.message
                    editMessage.replyMarkup = it.markup as InlineKeyboardMarkup?
                    execute(editMessage)
                }
            }
        }
    }

    /**
     * Switches bot state to next one.
     */
    private fun switchState(chatId: Long) {
        if (exists(chatId, RedisParams.STATE.name)) {
            val command = getValue(chatId, RedisParams.COMMAND.name)!!
            setValue(chatId, RedisParams.STATE.name, CommandsMap.get(command).nextState(chatId).name)
        }
    }
}