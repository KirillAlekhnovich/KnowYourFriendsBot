package com.backend.kyf.service

import com.backend.kyf.dto.AttributeDTO
import com.backend.kyf.dto.FriendDTO
import com.backend.kyf.entity.Friend
import com.backend.kyf.repository.FriendRepository
import com.backend.kyf.utils.FriendMapper
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class FriendService(
    private val friendRepository: FriendRepository,
    private val friendMapper: FriendMapper
) {

    fun createFriend(friendDTO: FriendDTO): FriendDTO {
        val friend = friendMapper.toEntity(friendDTO)
        friendRepository.save(friend)
        return friendMapper.toDTO(friend)
    }

    fun getFriendById(friendId: Long): Friend {
        return friendRepository.findByIdOrNull(friendId)
            ?: throw RuntimeException("Couldn't find user with id $friendId")
    }

    fun getFriendDTOById(friendId: Long): FriendDTO {
        return friendMapper.toDTO(getFriendById(friendId))
    }

    fun updateFriend(friendId: Long, newFriendDTO: FriendDTO): FriendDTO {
        val modifiedFriend = friendRepository.findByIdOrNull(friendId)
            ?: throw RuntimeException("Couldn't find user with id $friendId")
        // TODO()
        return friendMapper.toDTO(modifiedFriend)
    }

    fun deleteFriend(friendId: Long) {
        friendRepository.deleteById(friendId)
    }

    fun addAttributeToFriend(friendId: Long, attributeDTO: AttributeDTO): FriendDTO {
        val modifiedFriend = friendRepository.findByIdOrNull(friendId)
            ?: throw RuntimeException("Couldn't find user with id $friendId")
        modifiedFriend.attributes?.set(attributeDTO.name, attributeDTO.value)
        friendRepository.save(modifiedFriend)
        return friendMapper.toDTO(modifiedFriend)
    }

    fun deleteAttributeFromFriend(friendId: Long, attributeName: String): FriendDTO {
        val modifiedFriend = friendRepository.findByIdOrNull(friendId)
            ?: throw RuntimeException("Couldn't find user with id $friendId")
        modifiedFriend.attributes?.remove(attributeName)
        friendRepository.save(modifiedFriend)
        return friendMapper.toDTO(modifiedFriend)
    }
}