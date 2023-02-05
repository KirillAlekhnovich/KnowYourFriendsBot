package com.telegram.bot.utils

import com.telegram.bot.dto.TelegramBotStateDTO
import com.telegram.bot.handler.BotCommandHandler
import com.telegram.bot.handler.BotState
import com.telegram.bot.service.FriendRequestService
import com.telegram.bot.service.TelegramBotStateService
import com.telegram.bot.service.UserRequestService
import com.telegram.bot.utils.CommandParser.isCommand
import com.telegram.bot.utils.CommandParser.toCommand
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import java.util.*

@Service
class TelegramBotConfig(
    private val telegramBotStateService: TelegramBotStateService,
    private val userRequestService: UserRequestService,
    private val friendRequestService: FriendRequestService
) : TelegramLongPollingBot() {

    @Value("\${telegram.bot.token}")
    private lateinit var botToken: String

    @Value("\${telegram.bot.name}")
    private lateinit var botUsername: String

    override fun getBotToken(): String = botToken

    override fun getBotUsername(): String = botUsername

    override fun onUpdateReceived(update: Update) {
        val (chatId, message) = getMessage(update)
        if (!userRequestService.exists(chatId)) userRequestService.registerUser(chatId)
        val botState = if (telegramBotStateService.exists(chatId)) {
            telegramBotStateService.getBotStateById(chatId)
        } else {
            telegramBotStateService.createBotState(
                TelegramBotStateDTO(chatId, BotCommandHandler.START,
                BotState.EXPECTING_COMMAND, emptyMap<String, String>().toMutableMap())
            )
        }
        val user = userRequestService.getUser(botState.id)
        if (message.isCommand()) {
            botState.state = BotState.EXPECTING_COMMAND
            botState.command = message.toCommand()
        }
        val response = botState.command.execute(user, botState, message, userRequestService, friendRequestService)
        val markup = botState.command.generateButtons(botState.state)
        sendResponse(botState.id, response, markup)
        botState.state = botState.command.nextState(botState.state)
        telegramBotStateService.createBotState(botState)
    }

    private fun getMessage(update: Update): Pair<Long, String> {
        val message: String
        val chatId: Long
        if (update.hasCallbackQuery()) {
            message = update.callbackQuery.data
            chatId = update.callbackQuery.from.id
        } else if (update.hasMessage()) {
            message = update.message.text
            chatId = update.message.chatId
        } else throw RuntimeException("No message received")
        return Pair(chatId, message)
    }

    private fun sendResponse(chatId: Long, response: String, replyMarkup: ReplyKeyboard?) {
        val responseMsg = SendMessage(chatId.toString(), response)
        if (replyMarkup != null) responseMsg.replyMarkup = replyMarkup
        execute(responseMsg)
    }
}