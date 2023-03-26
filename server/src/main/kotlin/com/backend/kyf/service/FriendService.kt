package com.backend.kyf.service

import com.backend.kyf.dto.AttributeDTO
import com.backend.kyf.dto.FriendDTO
import com.backend.kyf.entity.Friend
import com.backend.kyf.exception.*
import com.backend.kyf.repository.FriendRepository
import com.backend.kyf.repository.UserRepository
import com.backend.kyf.utils.CorrectnessChecker.nameIsCorrect
import com.backend.kyf.utils.mapper.FriendMapper
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class FriendService(
    private val friendRepository: FriendRepository,
    private val friendMapper: FriendMapper,
    private val userRepository: UserRepository,
    private val userService: UserService
) {

    fun addFriend(userId: Long, friendDTO: FriendDTO): FriendDTO {
        val user = userService.getUserById(userId)
        if (!friendDTO.name.nameIsCorrect()) throw InvalidFriendNameException()
        if (user.friends.any { it.name == friendDTO.name }) throw FriendAlreadyExistsException()
        val friend = friendMapper.toEntity(friendDTO)
        friendRepository.save(friend)
        user.generalAttributes.forEach {
            addAttribute(userId, friend.id, AttributeDTO(it, "Not set"))
        }
        user.friends.add(friend)
        userRepository.save(user)
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

    fun getFriendByName(userId: Long, friendName: String): FriendDTO {
        val user = userService.getUserById(userId)
        val friend = user.friends.find { it.name == friendName } ?: throw FriendDoesNotExistException()
        return friendMapper.toDTO(friend)
    }

    fun getAllFriends(userId: Long): List<FriendDTO> {
        val user = userService.getUserById(userId)
        return user.friends.map { friendMapper.toDTO(it) }.sortedBy { it.name }
    }

    fun getAllFriendNames(userId: Long): List<String> {
        val user = userService.getUserById(userId)
        return user.friends.map { it.name }.sorted()
    }

    fun updateFriend(userId: Long, friendId: Long, newFriendDTO: FriendDTO): FriendDTO {
        val modifiedFriend = getFriendById(userId, friendId)
        // TODO()
        return friendMapper.toDTO(modifiedFriend)
    }

    fun changeFriendsName(userId: Long, friendId: Long, newName: String): FriendDTO {
        val user = userService.getUserById(userId)
        val modifiedFriend = getFriendById(userId, friendId)
        if (!newName.nameIsCorrect()) throw InvalidFriendNameException()
        if (user.friends.any { it.name == newName }) throw FriendAlreadyExistsException()
        modifiedFriend.name = newName
        friendRepository.save(modifiedFriend)
        return friendMapper.toDTO(modifiedFriend)
    }

    fun removeFriend(userId: Long, friendId: Long) {
        val user = userService.getUserById(userId)
        val friend = getFriendById(userId, friendId)
        friendRepository.deleteById(friendId)
        user.friends.remove(friend)
    }

    fun addAttribute(userId: Long, friendId: Long, attributeDTO: AttributeDTO): FriendDTO {
        val friend = getFriendById(userId, friendId)
        if (!attributeDTO.name.nameIsCorrect()) throw InvalidAttributeNameException()
        if (hasAttribute(userId, friendId, attributeDTO.name)) throw AttributeAlreadyExistsException()
        friend.attributes[attributeDTO.name] = attributeDTO.value
        friendRepository.save(friend)
        return friendMapper.toDTO(friend)
    }

    fun hasAttribute(userId: Long, friendId: Long, attributeName: String): Boolean {
        val friend = getFriendById(userId, friendId)
        return friend.attributes.containsKey(attributeName)
    }

    fun getAttributes(userId: Long, friendId: Long): List<AttributeDTO> {
        val modifiedFriend = getFriendById(userId, friendId)
        val attributes = mutableListOf<AttributeDTO>()
        modifiedFriend.attributes.forEach { attributes.add(AttributeDTO(it.key, it.value)) }
        return attributes.sortedBy { it.name }
    }

    fun getAttributeNames(userId: Long, friendId: Long): List<String> {
        val modifiedFriend = getFriendById(userId, friendId)
        val attributeNames = mutableListOf<String>()
        modifiedFriend.attributes.forEach { attributeNames.add(it.key) }
        return attributeNames.sorted()
    }

    fun updateAttribute(userId: Long, friendId: Long, attributeDTO: AttributeDTO): FriendDTO {
        val friend = getFriendById(userId, friendId)
        if (!hasAttribute(userId, friendId, attributeDTO.name)) throw AttributeDoesNotExistException()
        friend.attributes[attributeDTO.name] = attributeDTO.value
        friendRepository.save(friend)
        return friendMapper.toDTO(friend)
    }

    fun removeAttribute(userId: Long, friendId: Long, attributeName: String): FriendDTO {
        val friend = getFriendById(userId, friendId)
        if (!hasAttribute(userId, friendId, attributeName)) throw AttributeDoesNotExistException()
        friend.attributes.remove(attributeName)
        friendRepository.save(friend)
        return friendMapper.toDTO(friend)
    }
}