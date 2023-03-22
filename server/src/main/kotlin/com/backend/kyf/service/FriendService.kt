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
import org.springframework.context.annotation.Lazy

@Service
class FriendService(
    private val friendRepository: FriendRepository,
    private val friendMapper: FriendMapper,
    @Lazy private val userService: UserService
) {

    fun createFriend(friendDTO: FriendDTO): FriendDTO {
        val friend = friendMapper.toEntity(friendDTO)
        friendRepository.save(friend)
        return friendMapper.toDTO(friend)
    }

    fun getFriendById(userId: Long, friendId: Long): Friend {
        val friend = friendRepository.findByIdOrNull(friendId) ?: throw FriendDoesNotExistException()
        if (friend.ownerId != userId) throw AccessDeniedException()
        return friend
    }

    fun getFriendDTOById(userId: Long, friendId: Long): FriendDTO {
        return friendMapper.toDTO(getFriendById(userId, friendId))
    }

    fun updateFriend(userId: Long, friendId: Long, newFriendDTO: FriendDTO): FriendDTO {
        val modifiedFriend = getFriendById(userId, friendId)
        // TODO()
        return friendMapper.toDTO(modifiedFriend)
    }

    fun deleteFriend(userId: Long, friendId: Long) {
        val friend = friendRepository.findByIdOrNull(friendId) ?: throw FriendDoesNotExistException()
        if (friend.ownerId != userId) throw AccessDeniedException()
        friendRepository.deleteById(friendId)
    }

    fun changeFriendsName(userId: Long, friendId: Long, newName: String): FriendDTO {
        val user = userService.getUserById(userId)
        val modifiedFriend = getFriendById(userId, friendId)
        if (!newName.isCorrect()) throw InvalidFriendNameException()
        if (user.friends.any { it.name == newName }) throw FriendAlreadyExistsException()
        modifiedFriend.name = newName
        friendRepository.save(modifiedFriend)
        return friendMapper.toDTO(modifiedFriend)
    }

    fun addAttribute(userId: Long, friendId: Long, attributeDTO: AttributeDTO): FriendDTO {
        val modifiedFriend = getFriendById(userId, friendId)
        if (!attributeDTO.name.isCorrect()) throw InvalidAttributeNameException()
        if (hasAttribute(userId, friendId, attributeDTO.name)) throw AttributeAlreadyExistsException()
        modifiedFriend.attributes[attributeDTO.name] = attributeDTO.value
        friendRepository.save(modifiedFriend)
        return friendMapper.toDTO(modifiedFriend)
    }

    fun getAttributes(userId: Long, friendId: Long): List<AttributeDTO> {
        val modifiedFriend = getFriendById(userId, friendId)
        val attributes = mutableListOf<AttributeDTO>()
        for ((name, value) in modifiedFriend.attributes) {
            attributes.add(AttributeDTO(name, value))
        }
        return attributes.sortedBy { it.name }
    }

    fun getAttributeNames(userId: Long, friendId: Long): List<String> {
        val modifiedFriend = getFriendById(userId, friendId)
        val attributeNames = mutableListOf<String>()
        for ((name, _) in modifiedFriend.attributes) {
            attributeNames.add(name)
        }
        return attributeNames.sorted()
    }

    fun updateAttribute(userId: Long, friendId: Long, attributeDTO: AttributeDTO): FriendDTO {
        val modifiedFriend = getFriendById(userId, friendId)
        if (!hasAttribute(userId, friendId, attributeDTO.name)) throw AttributeDoesNotExistException()
        modifiedFriend.attributes[attributeDTO.name] = attributeDTO.value
        friendRepository.save(modifiedFriend)
        return friendMapper.toDTO(modifiedFriend)
    }

    fun hasAttribute(userId: Long, friendId: Long, attributeName: String): Boolean {
        val modifiedFriend = getFriendById(userId, friendId)
        return modifiedFriend.attributes.containsKey(attributeName)
    }

    fun deleteAttribute(userId: Long, friendId: Long, attributeName: String): FriendDTO {
        val modifiedFriend = getFriendById(userId, friendId)
        if (!hasAttribute(userId, friendId, attributeName)) throw AttributeDoesNotExistException()
        modifiedFriend.attributes.remove(attributeName)
        friendRepository.save(modifiedFriend)
        return friendMapper.toDTO(modifiedFriend)
    }
}