package com.telegram.bot.utils

import com.telegram.bot.entity.TelegramBotState
import com.telegram.bot.dto.TelegramBotStateDTO
import org.springframework.stereotype.Component

@Component
class TelegramBotStateMapper {
    fun toDTO(entity: TelegramBotState): TelegramBotStateDTO {
        return TelegramBotStateDTO(
            entity.userId,
            entity.currCommand,
            entity.state,
            entity.commandsQueue,
            entity.storage
        )
    }

    fun toEntity(dto: TelegramBotStateDTO): TelegramBotState {
        return TelegramBotState(
            dto.userId,
            dto.currCommand,
            dto.state,
            dto.commandsQueue,
            dto.storage
        )
    }
}