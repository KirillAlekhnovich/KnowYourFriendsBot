package com.backend.kyf.bot

import com.backend.kyf.dto.UserBotDTO
import com.backend.kyf.service.FriendService
import com.backend.kyf.service.UserBotService
import com.backend.kyf.service.UserService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import java.util.*

@Service
class TelegramBot(
    private val userBotService: UserBotService,
    private val friendService: FriendService,
    private val userService: UserService,
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
        if (!userService.exists(chatId)) userService.registerUser(chatId)
        val userBot = if (userBotService.exists(chatId)) {
            userBotService.getUserBotById(chatId)
        } else {
            userBotService.createUserBot(UserBotDTO(chatId, BotCommand.START,
                BotState.EXPECTING_COMMAND, emptyMap<String, String>().toMutableMap()))
        }
        val user = userService.getUserById(userBot.id)
        if (message.text == "/cancel") userBot.command = BotCommand.CANCEL
        sendResponse(userBot.id, userBot.state.execute(user, userBot, message, userService, friendService))
        userBotService.createUserBot(userBot)
    }

    private fun sendResponse(chatId: Long, response: String) {
        val responseMsg = SendMessage(chatId.toString(), response)
        execute(responseMsg)
    }
}