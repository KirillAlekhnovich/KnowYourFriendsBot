package com.backend.kyf.utils

import com.backend.kyf.dto.FriendSlimDTO
import com.backend.kyf.dto.UserDTO
import com.backend.kyf.entity.User
import org.springframework.stereotype.Component

@Component
class UserMapper: Mapper<UserDTO, User> {
    override fun toDTO(entity: User): UserDTO {
        val friendDTOs: MutableSet<FriendSlimDTO> = entity.friends?.map {
            FriendSlimDTO(it.id, it.name)
        }?.toMutableSet() ?: throw RuntimeException("Error during friends to friendsSlimDTO conversion")
        return UserDTO(
            entity.id,
            friendDTOs,
            entity.generalAttributes
        )
    }

    override fun toEntity(dto: UserDTO): User {
        TODO("Not yet implemented")
    }
}