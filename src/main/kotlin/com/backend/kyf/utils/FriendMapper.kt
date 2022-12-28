package com.backend.kyf.utils

import com.backend.kyf.dto.FriendDTO
import com.backend.kyf.entity.Friend
import org.springframework.stereotype.Component

@Component
class FriendMapper: Mapper<FriendDTO, Friend> {

    override fun toDTO(entity: Friend): FriendDTO {
        return FriendDTO(
            entity.id,
            entity.name,
            entity.birthdayDate,
            entity.attributes
        )
    }

    override fun toEntity(dto: FriendDTO): Friend {
        return Friend(
            dto.id,
            dto.name,
            dto.birthdayDate,
            dto.attributes
        )
    }
}