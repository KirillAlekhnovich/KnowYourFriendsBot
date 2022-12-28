package com.backend.kyf.service

import com.backend.kyf.dto.FriendDTO
import com.backend.kyf.dto.UserDTO
import com.backend.kyf.entity.User
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
    private val friendService: FriendService
) {

    fun registerUser(userId: Long): UserDTO {
        val user = User(userId, null)
        userRepository.save(user)
        return userMapper.toDTO(user)
    }

    fun addFriend(userId: Long, friendDTO: FriendDTO): UserDTO {
        val user = userRepository.findByIdOrNull(userId) ?: throw RuntimeException("User with id $userId does not exist")
        user.friends?.add(friendMapper.toEntity(friendService.createFriend(friendDTO)))
        userRepository.save(user)
        return userMapper.toDTO(user)
    }

    fun removeFriend(userId: Long, friendId: Long): UserDTO {
        val user = userRepository.findByIdOrNull(userId) ?: throw RuntimeException("User with id $userId does not exist")
        val friend = friendMapper.toEntity(friendService.getFriendById(friendId))
        user.friends?.remove(friend)
        friendService.deleteFriend(friendId)
        userRepository.save(user)
        return userMapper.toDTO(user)
    }
}