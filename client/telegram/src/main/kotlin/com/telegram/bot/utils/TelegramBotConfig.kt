package com.telegram.bot.utils

import com.telegram.bot.dto.UserDTO
import com.telegram.bot.handler.BotState
import com.telegram.bot.handler.CommandHandler
import com.telegram.bot.service.UserRequestService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import java.util.*

@Service
class TelegramBotConfig(
    private val userRequestService: UserRequestService,
    private val commandHandler: CommandHandler
) : TelegramLongPollingBot() {

    @Value("\${telegram.bot.token}")
    private lateinit var botToken: String

    @Value("\${telegram.bot.name}")
    private lateinit var botUsername: String

    override fun getBotToken(): String = botToken

    override fun getBotUsername(): String = botUsername

    override fun onUpdateReceived(update: Update) {
        val (chatId, message, messageId) = getMessage(update)
        val user = getUser(chatId)
        val jedis = Jedis.get()
        redisInitialSetup(chatId, messageId)
        sendResponse(user, message)
        if (jedis.hexists(chatId.toString(), RedisParams.STATE.name)) {
            val command = jedis.hget(chatId.toString(), RedisParams.COMMAND.name)
            jedis.hset(chatId.toString(), RedisParams.STATE.name, CommandsMap.get(command).nextState(chatId).name)
        }
    }

    private fun getMessage(update: Update): Triple<Long, String, String> {
        val chatId: Long
        val message: String
        val messageId: String
        if (update.hasCallbackQuery()) {
            message = update.callbackQuery.data
            chatId = update.callbackQuery.from.id
            messageId = update.callbackQuery.message.messageId.toString()
        } else if (update.hasMessage()) {
            message = update.message.text
            chatId = update.message.chatId
            messageId = update.message.messageId.toString()
        } else throw RuntimeException("No message received")
        return Triple(chatId, message, messageId)
    }

    private fun getUser(chatId: Long): UserDTO {
        if (!userRequestService.exists(chatId)) {
            Jedis.get().hset(chatId.toString(), RedisParams.ACCESS_TOKEN.name, userRequestService.registerUser(chatId))
        }
        return userRequestService.getUser(chatId)
    }

    private fun redisInitialSetup(chatId: Long, messageId: String) {
        val jedis = Jedis.get()
        if (!jedis.hexists(chatId.toString(), RedisParams.COMMAND.name)) {
            jedis.hset(chatId.toString(), RedisParams.COMMAND.name, Commands.START)
        }
        if (!jedis.hexists(chatId.toString(), RedisParams.STATE.name)) {
            jedis.hset(chatId.toString(), RedisParams.STATE.name, BotState.EXPECTING_COMMAND.name)
        }
        jedis.hset(chatId.toString(), RedisParams.CALLBACK_MESSAGE_ID.name, messageId)
    }

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
                    editMessage.messageId = Jedis.get().hget(user.id.toString(), RedisParams.CALLBACK_MESSAGE_ID.name).toInt()
                    editMessage.text = it.message
                    editMessage.replyMarkup = it.markup as InlineKeyboardMarkup?
                    execute(editMessage)
                }
            }
        }
    }
}