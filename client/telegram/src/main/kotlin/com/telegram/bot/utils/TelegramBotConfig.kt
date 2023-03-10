package com.telegram.bot.utils

import com.telegram.bot.dto.TelegramBotStateDTO
import com.telegram.bot.dto.UserDTO
import com.telegram.bot.dto.addParamToStorage
import com.telegram.bot.dto.getParamFromStorage
import com.telegram.bot.handler.BotState
import com.telegram.bot.handler.CommandHandler
import com.telegram.bot.service.TelegramBotStateService
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
    private val telegramBotStateService: TelegramBotStateService,
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
        val (user, botState) = getConfig(chatId)
        botState.addParamToStorage(StorageParams.CALLBACK_MESSAGE_ID, messageId)
        sendResponse(botState.userId, botState, user, message)
        botState.state = CommandsMap.get(botState.currCommand).nextState(botState)
        if (botState.state == BotState.EXPECTING_COMMAND) botState.currCommand = Commands.UNKNOWN
        telegramBotStateService.createBotState(botState)
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

    private fun getConfig(chatId: Long): Pair<UserDTO, TelegramBotStateDTO> {
        val user = if (!userRequestService.exists(chatId)) userRequestService.registerUser(chatId)
        else userRequestService.getUser(chatId)
        val botState = if (telegramBotStateService.exists(chatId)) {
            telegramBotStateService.getBotStateById(chatId)
        } else {
            telegramBotStateService.createBotState(
                TelegramBotStateDTO(
                    chatId, Commands.START,
                    BotState.EXPECTING_COMMAND, mutableListOf(), emptyMap<String, String>().toMutableMap()
                )
            )
        }
        return Pair(user, botState)
    }

    private fun sendResponse(chatId: Long, botState: TelegramBotStateDTO, user: UserDTO, message: String) {
        val response = commandHandler.handle(user, message, botState)
        response.forEach {
            when (it.type) {
                MessageType.SEND -> {
                    val responseMsg = SendMessage(chatId.toString(), it.message)
                    responseMsg.replyMarkup = it.markup
                    execute(responseMsg)
                }
                MessageType.EDIT -> {
                    val editMessage = EditMessageText()
                    editMessage.chatId = chatId.toString()
                    editMessage.messageId = botState.getParamFromStorage(StorageParams.CALLBACK_MESSAGE_ID).toInt()
                    editMessage.text = it.message
                    editMessage.replyMarkup = it.markup as InlineKeyboardMarkup?
                    execute(editMessage)
                }
            }
        }
    }
}