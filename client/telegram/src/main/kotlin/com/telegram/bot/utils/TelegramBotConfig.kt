package com.telegram.bot.utils

import com.telegram.bot.dto.TelegramBotStateDTO
import com.telegram.bot.handler.BotCommandHandler
import com.telegram.bot.handler.BotStateHandler
import com.telegram.bot.service.FriendRequestService
import com.telegram.bot.service.TelegramBotStateService
import com.telegram.bot.service.UserRequestService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
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
        if (!update.hasMessage()) throw RuntimeException("No message received")
        val message = update.message
        val chatId = message.chatId
        if (!userRequestService.exists(chatId)) userRequestService.registerUser(chatId)
        val botState = if (telegramBotStateService.exists(chatId)) {
            telegramBotStateService.getBotStateById(chatId)
        } else {
            telegramBotStateService.createBotState(
                TelegramBotStateDTO(chatId, BotCommandHandler.START,
                BotStateHandler.EXPECTING_COMMAND, emptyMap<String, String>().toMutableMap())
            )
        }
        val user = userRequestService.getUser(botState.id)
        if (message.text == "/cancel") botState.command = BotCommandHandler.CANCEL
        sendResponse(botState.id, botState.state.execute(user, botState, message, userRequestService, friendRequestService))
        telegramBotStateService.createBotState(botState)
    }

    private fun sendResponse(chatId: Long, response: String) {
        val responseMsg = SendMessage(chatId.toString(), response)
        execute(responseMsg)
    }
}