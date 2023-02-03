package com.telegram.bot.utils

import com.telegram.bot.entity.TelegramBotState
import com.telegram.bot.dto.TelegramBotStateDTO
import org.springframework.stereotype.Component

@Component
class TelegramBotStateMapper {
    fun toDTO(entity: TelegramBotState): TelegramBotStateDTO {
        return TelegramBotStateDTO(
            entity.id,
            entity.command,
            entity.state,
            entity.storage
        )
    }

    fun toEntity(dto: TelegramBotStateDTO): TelegramBotState {
        return TelegramBotState(
            dto.id,
            dto.command,
            dto.state,
            dto.storage
        )
    }
}