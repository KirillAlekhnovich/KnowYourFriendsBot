package com.backend.kyf.utils

import com.backend.kyf.dto.UserBotDTO
import com.backend.kyf.entity.UserBot
import org.springframework.stereotype.Component

@Component
class UserBotMapper: Mapper<UserBotDTO, UserBot> {
    override fun toDTO(entity: UserBot): UserBotDTO {
        return UserBotDTO(
            entity.id,
            entity.command,
            entity.state,
            entity.storage
        )
    }

    override fun toEntity(dto: UserBotDTO): UserBot {
        return UserBot(
            dto.id,
            dto.command,
            dto.state,
            dto.storage
        )
    }
}