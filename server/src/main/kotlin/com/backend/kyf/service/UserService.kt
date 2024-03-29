package com.backend.kyf.service

import com.backend.kyf.dto.UserDTO
import com.backend.kyf.entity.Friend
import com.backend.kyf.entity.User
import com.backend.kyf.exception.*
import com.backend.kyf.repository.FriendRepository
import com.backend.kyf.repository.UserRepository
import com.backend.kyf.utils.auth.AuthService
import com.backend.kyf.utils.CorrectnessChecker.nameIsCorrect
import com.backend.kyf.utils.RedisParams
import com.backend.kyf.utils.auth.Jedis.setValue
import com.backend.kyf.utils.mapper.UserMapper
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.context.annotation.Lazy

/**
 * Service for handling user-related requests.
 */
@Service
class UserService(
    private val userMapper: UserMapper,
    private val userRepository: UserRepository,
    private val friendRepository: FriendRepository,
    private val authService: AuthService,
    @Lazy private val friendService: FriendService
) {

    /**
     * Registers a new user.
     */
    fun registerUser(userId: Long): UserDTO {
        if (exists(userId)) throw UserAlreadyExistsException()
        val user = User(userId, emptySet<Friend>().toMutableSet(), emptySet<String>().toMutableSet())
        setValue(userId, RedisParams.ACCESS_TOKEN.name, authService.generateAccessToken(userId))
        userRepository.save(user)
        return userMapper.toDTO(user)
    }

    /**
     * Gets user entity by id.
     */
    fun getUserById(userId: Long): User {
        return userRepository.findByIdOrNull(userId) ?: throw UserDoesNotExistException()
    }

    /**
     * Gets user dto by id.
     */
    fun getUserDTOById(userId: Long): UserDTO {
        return userMapper.toDTO(getUserById(userId))
    }

    /**
     * Checks if user exists.
     */
    fun exists(userId: Long): Boolean {
        return userRepository.existsById(userId)
    }

    /**
     * Resets user's profile. Deletes all friends and general attributes.
     */
    fun reset(userId: Long) {
        val user = getUserById(userId)
        user.friends.forEach {
            friendService.removeFriend(userId, it.id)
        }
        user.friends.clear()
        user.generalAttributes.clear()
        userRepository.save(user)
    }

    /**
     * Adds a general attribute which will be added to all user's friends.
     */
    fun addGeneralAttribute(userId: Long, attributeName: String) {
        val user = getUserById(userId)
        if (!attributeName.nameIsCorrect()) throw InvalidAttributeNameException()
        if (hasGeneralAttribute(userId, attributeName)) throw AttributeAlreadyExistsException()
        user.friends.forEach {
            if (!it.attributes.containsKey(attributeName)) {
                it.attributes[attributeName] = "Not set"
                friendRepository.save(it)
            }
        }
        user.generalAttributes.add(attributeName)
        userRepository.save(user)
    }

    /**
     * Checks if user has a general attribute.
     */
    fun hasGeneralAttribute(userId: Long, attributeName: String): Boolean {
        val user = getUserById(userId)
        return user.generalAttributes.contains(attributeName)
    }

    /**
     * Removes a general attribute from user's friends.
     */
    fun removeGeneralAttribute(userId: Long, attributeName: String) {
        if (!hasGeneralAttribute(userId, attributeName)) throw AttributeDoesNotExistException()
        val user = getUserById(userId)
        user.friends.forEach {
            it.attributes.remove(attributeName)
            friendRepository.save(it)
        }
        user.generalAttributes.remove(attributeName)
        userRepository.save(user)
    }
}