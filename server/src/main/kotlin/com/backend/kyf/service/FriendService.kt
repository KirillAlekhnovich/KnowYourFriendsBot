package com.backend.kyf.service

import com.backend.kyf.dto.AttributeDTO
import com.backend.kyf.dto.FriendDTO
import com.backend.kyf.entity.Friend
import com.backend.kyf.exception.*
import com.backend.kyf.repository.FriendRepository
import com.backend.kyf.utils.CorrectnessChecker.isCorrect
import com.backend.kyf.utils.mapper.FriendMapper
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
        return friendRepository.findByIdOrNull(friendId) ?: throw FriendDoesNotExistException()
    }

    fun getFriendDTOById(friendId: Long): FriendDTO {
        return friendMapper.toDTO(getFriendById(friendId))
    }

    fun updateFriend(friendId: Long, newFriendDTO: FriendDTO): FriendDTO {
        val modifiedFriend = getFriendById(friendId)
        // TODO()
        return friendMapper.toDTO(modifiedFriend)
    }

    fun deleteFriend(friendId: Long) {
        friendRepository.deleteById(friendId)
    }

    fun addAttribute(friendId: Long, attributeDTO: AttributeDTO): FriendDTO {
        val modifiedFriend = getFriendById(friendId)
        if (!attributeDTO.name.isCorrect()) throw InvalidAttributeNameException()
        if (hasAttribute(friendId, attributeDTO.name)) throw AttributeAlreadyExistsException()
        modifiedFriend.attributes[attributeDTO.name] = attributeDTO.value
        friendRepository.save(modifiedFriend)
        return friendMapper.toDTO(modifiedFriend)
    }

    fun getAttributes(friendId: Long): List<AttributeDTO> {
        val modifiedFriend = getFriendById(friendId)
        val attributes = mutableListOf<AttributeDTO>()
        for ((name, value) in modifiedFriend.attributes) {
            attributes.add(AttributeDTO(name, value))
        }
        return attributes
    }

    fun getAttributeNames(friendId: Long): List<String> {
        val modifiedFriend = getFriendById(friendId)
        val attributeNames = mutableListOf<String>()
        for ((name, _) in modifiedFriend.attributes) {
            attributeNames.add(name)
        }
        return attributeNames
    }

    fun updateAttribute(friendId: Long, attributeDTO: AttributeDTO): FriendDTO {
        val modifiedFriend = getFriendById(friendId)
        if (!hasAttribute(friendId, attributeDTO.name)) throw AttributeDoesNotExistException()
        modifiedFriend.attributes[attributeDTO.name] = attributeDTO.value
        friendRepository.save(modifiedFriend)
        return friendMapper.toDTO(modifiedFriend)
    }

    fun hasAttribute(friendId: Long, attributeName: String): Boolean {
        val modifiedFriend = getFriendById(friendId)
        return modifiedFriend.attributes.containsKey(attributeName)
    }

    fun deleteAttribute(friendId: Long, attributeName: String): FriendDTO {
        val modifiedFriend = getFriendById(friendId)
        if (!hasAttribute(friendId, attributeName)) throw AttributeDoesNotExistException()
        modifiedFriend.attributes.remove(attributeName)
        friendRepository.save(modifiedFriend)
        return friendMapper.toDTO(modifiedFriend)
    }
}