package com.backend.kyf.bot

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update

@Service
class TelegramBot: TelegramLongPollingBot() {

    @Value("\${telegram.bot.token}")
    private lateinit var botToken: String

    @Value("\${telegram.bot.name}")
    private lateinit var botUsername: String

    override fun getBotToken(): String = botToken

    override fun getBotUsername(): String = botUsername

    override fun onUpdateReceived(update: Update) {
        if (update.hasMessage()) {
            val message = update.message
            val chatId = message.chatId
            val response = when (message.text) {
                "/start" -> "Hi! I'm KYF bot."
                else -> "You've written: ${message.text}"
            }
            sendResponse(chatId, response)
        }
    }

    private fun sendResponse(chatId: Long, response: String) {
        val responseMsg = SendMessage(chatId.toString(), response)
        execute(responseMsg)
    }
}