package com.backend.kyf.utils.mapper

import com.backend.kyf.dto.FriendDTO
import com.backend.kyf.dto.UserDTO
import com.backend.kyf.entity.User
import org.springframework.stereotype.Component

@Component
class UserMapper: Mapper<UserDTO, User> {
    override fun toDTO(entity: User): UserDTO {
        return UserDTO(
            entity.id,
            entity.friends.map { FriendDTO(it.id, it.name, it.attributes) }.toMutableSet(),
            entity.generalAttributes
        )
    }

    override fun toEntity(dto: UserDTO): User {
        TODO("Not yet implemented")
    }
}