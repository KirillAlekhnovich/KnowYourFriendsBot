package com.telegram.bot.service

import com.telegram.bot.dto.TelegramBotStateDTO
import com.telegram.bot.repository.TelegramBotStateRepository
import com.telegram.bot.utils.TelegramBotStateMapper
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class TelegramBotStateService(
    private val telegramBotStateRepository: TelegramBotStateRepository,
    private val telegramBotStateMapper: TelegramBotStateMapper
) {

    fun createBotState(telegramBotStateDTO: TelegramBotStateDTO): TelegramBotStateDTO {
        val botState = telegramBotStateMapper.toEntity(telegramBotStateDTO)
        telegramBotStateRepository.save(botState)
        return telegramBotStateMapper.toDTO(botState)
    }

    fun getBotStateById(chatId: Long): TelegramBotStateDTO {
        val botState = telegramBotStateRepository.findByIdOrNull(chatId)
        return telegramBotStateMapper.toDTO(botState!!)
    }

    fun exists(chatId: Long): Boolean {
        return telegramBotStateRepository.existsById(chatId)
    }
}