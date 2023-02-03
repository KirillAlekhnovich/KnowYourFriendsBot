package com.backend.kyf.service

import com.backend.kyf.dto.AttributeDTO
import com.backend.kyf.dto.FriendDTO
import com.backend.kyf.dto.UserDTO
import com.backend.kyf.entity.Friend
import com.backend.kyf.entity.User
import com.backend.kyf.repository.FriendRepository
import com.backend.kyf.repository.UserRepository
import com.backend.kyf.utils.FriendMapper
import com.backend.kyf.utils.UserMapper
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userMapper: UserMapper,
    private val friendMapper: FriendMapper,
    private val friendService: FriendService,
    private val friendRepository: FriendRepository
) {

    fun registerUser(userId: Long): UserDTO {
        val user = User(userId, emptySet<Friend>().toMutableSet(), emptySet<String>().toMutableSet())
        userRepository.save(user)
        return userMapper.toDTO(user)
    }

    fun getUserById(userId: Long): User {
        return userRepository.findByIdOrNull(userId) ?: throw RuntimeException("User with id $userId does not exist")
    }

    fun getUserDTOById(userId: Long): UserDTO {
        return userMapper.toDTO(getUserById(userId))
    }

    fun exists(userId: Long): Boolean {
        return userRepository.existsById(userId)
    }

    fun reset(userId: Long) {
        val user = getUserById(userId)
        user.friends.forEach {
            friendService.deleteFriend(it.id)
        }
        user.friends.clear()
        user.generalAttributes.clear()
        userRepository.save(user)
    }

    fun addFriend(userId: Long, friendDTO: FriendDTO): UserDTO {
        val user = getUserById(userId)
        val friend = friendMapper.toEntity(friendService.createFriend(friendDTO))
        if (friend.name[0] == '/') throw RuntimeException("Friend name cannot start with /")
        user.generalAttributes.forEach {
            friendService.addAttribute(friend.id, AttributeDTO(it, "Not set"))
        }
        if (user.friends.any { it.name == friend.name }) throw RuntimeException("User already has friend named ${friend.name}")
        user.friends.add(friend)
        userRepository.save(user)
        return userMapper.toDTO(user)
    }

    fun getAllFriends(userId: Long): List<FriendDTO> {
        val user = getUserById(userId)
        return user.friends.map { friendMapper.toDTO(it) }
    }

    fun addGeneralAttribute(userId: Long, attributeName: String) {
        val user = getUserById(userId)
        user.friends.forEach {
            if (!it.attributes.containsKey(attributeName)) {
                it.attributes[attributeName] = "Not set"
                friendRepository.save(it)
            }
        }
        user.generalAttributes.add(attributeName)
        userRepository.save(user)
    }

    fun hasGeneralAttribute(userId: Long, attributeName: String): Boolean {
        val user = getUserById(userId)
        return user.generalAttributes.contains(attributeName)
    }

    fun removeGeneralAttribute(userId: Long, attributeName: String) {
        val user = getUserById(userId)
        user.friends.forEach {
            it.attributes.remove(attributeName)
            friendRepository.save(it)
        }
        user.generalAttributes.remove(attributeName)
    }

    fun getFriendByName(userId: Long, friendName: String): FriendDTO? {
        val user = getUserById(userId)
        val friend = user.friends.find { it.name == friendName }
        return friend?.let { friendMapper.toDTO(it) }
    }

    fun removeFriend(userId: Long, friendId: Long): UserDTO {
        val user = getUserById(userId)
        val friend = friendService.getFriendById(friendId)
        friendService.deleteFriend(friendId)
        user.friends.remove(friend)
        return userMapper.toDTO(user)
    }
}