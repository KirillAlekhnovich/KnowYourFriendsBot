package com.backend.kyf.service

import com.backend.kyf.dto.UserDTO
import com.backend.kyf.entity.Friend
import com.backend.kyf.entity.User
import com.backend.kyf.exception.*
import com.backend.kyf.repository.FriendRepository
import com.backend.kyf.repository.UserRepository
import com.backend.kyf.utils.RedisParams
import com.backend.kyf.utils.auth.AuthService
import com.backend.kyf.utils.auth.Jedis
import com.backend.kyf.utils.mapper.UserMapper
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import org.springframework.data.repository.findByIdOrNull


class UserServiceTest {

    private val userRepository: UserRepository = mockk()
    private val friendRepository: FriendRepository = mockk()
    private val authService: AuthService = mockk()
    private val friendService: FriendService = mockk()
    private val userMapper: UserMapper = mockk()
    private val userService: UserService =
        UserService(userMapper, userRepository, friendRepository, authService, friendService)

    @BeforeEach
    fun setup() {
        clearAllMocks()
    }

    @Nested
    inner class RegisterUser {
        @Test
        fun `registering correct user`() {
            val userId = 1L
            val savedUser = User(userId, mutableSetOf(), mutableSetOf())
            val expectedUserDTO = UserDTO(userId, mutableSetOf(), mutableSetOf())
            val jedisMock = mockk<Jedis>()
            mockkObject(Jedis)

            every { userRepository.existsById(userId) } returns false
            every { authService.generateAccessToken(userId) } returns "access-token"
            every { jedisMock.setValue(any(), any(), any()) } just runs
            every { Jedis.setValue(userId, RedisParams.ACCESS_TOKEN.name, "access-token") } just runs
            every { Jedis.getValue(userId, RedisParams.ACCESS_TOKEN.name) } returns "access-token"
            every { userRepository.save(any()) } returns savedUser
            every { userMapper.toDTO(savedUser) } returns expectedUserDTO

            val actualUserDTO = userService.registerUser(userId)

            assertEquals(expectedUserDTO, actualUserDTO)

            verify(exactly = 1) { userRepository.existsById(userId) }
            verify(exactly = 1) { authService.generateAccessToken(userId) }
            verify(exactly = 1) { Jedis.setValue(userId, RedisParams.ACCESS_TOKEN.name, "access-token") }
            verify(exactly = 1) { userRepository.save(any()) }
            verify(exactly = 1) { userMapper.toDTO(savedUser) }
        }

        @Test
        fun `register when user already exists`() {
            val userId = 1L

            every { userRepository.existsById(userId) } returns true

            assertThrows<UserAlreadyExistsException> {
                userService.registerUser(userId)
            }

            verify(exactly = 1) { userRepository.existsById(userId) }
            verify(exactly = 0) { userRepository.save(any()) }
            verify(exactly = 0) { authService.generateAccessToken(any()) }
            verify(exactly = 0) { userMapper.toDTO(any()) }
        }
    }

    @Nested
    inner class GetUserById {
        @Test
        fun `getting user with valid user id`() {
            val user = User(1L, mutableSetOf(), mutableSetOf())

            every { userRepository.findByIdOrNull(user.id) } returns user

            val result = userService.getUserById(user.id)

            assertEquals(user, result)
        }

        @Test
        fun `getting user with an invalid user id`() {
            every { userRepository.findByIdOrNull(any()) } returns null

            assertThrows<UserDoesNotExistException> {
                userService.getUserById(1L)
            }
        }
    }

    @Nested
    inner class GetUserDTOById {
        @Test
        fun `getting user dto with a valid user id`() {
            val user = User(1L, mutableSetOf(), mutableSetOf())
            val userDTO = UserDTO(user.id, mutableSetOf(), mutableSetOf())

            every { userRepository.findByIdOrNull(user.id) } returns user
            every { userMapper.toDTO(user) } returns userDTO

            val result = userService.getUserDTOById(user.id)

            assertEquals(userMapper.toDTO(user), result)
        }
    }

    @Nested
    inner class Exists {
        @Test
        fun `exists returning true`() {
            every { userRepository.existsById(any()) } returns true

            val result = userService.exists(1L)

            assertTrue(result)
        }

        @Test
        fun `exists returning false`() {
            every { userRepository.existsById(any()) } returns false

            val result = userService.exists(1L)

            assertFalse(result)
        }
    }

    @Nested
    inner class Reset {
        @Test
        fun `correctly resetting user profile (deleting friends and attributes)`() {
            val userId = 1L
            val friend1 = Friend(2L, "Johny", userId, mutableMapOf())
            val friend2 = Friend(3L, "Billy", userId, mutableMapOf())
            val user = User(userId, mutableSetOf(friend1, friend2), mutableSetOf("Age", "Favorite artist"))

            every { userRepository.findByIdOrNull(userId) } returns user
            every { friendService.removeFriend(userId, any()) } just runs
            every { userRepository.save(user) } returns user

            userService.reset(userId)

            verify { friendService.removeFriend(userId, friend1.id) }
            verify { friendService.removeFriend(userId, friend2.id) }
            assertThat(user.friends).isEmpty()
            assertThat(user.generalAttributes).isEmpty()
            verify { userRepository.save(user) }
        }
    }

    @Nested
    inner class AddGeneralAttribute {
        @Test
        fun `correctly adding general attribute to user and his friends`() {
            val userId = 1L
            val attributeName = "New Attribute"
            val user = mockk<User>(relaxed = true) {
                every { friends } returns mutableSetOf(
                    mockk(relaxed = true) {
                        every { attributes } returns mutableMapOf("Old Attribute" to "value")
                    }
                )
                every { generalAttributes } returns mutableSetOf("Old Attribute")
            }

            every { userRepository.findByIdOrNull(userId) } returns user
            every { userRepository.save(any()) } answers { arg(0) }
            every { friendRepository.save(any()) } answers { arg(0) }

            userService.addGeneralAttribute(userId, attributeName)

            verify { userRepository.findByIdOrNull(userId) }
            verify { userRepository.save(user) }
            verify { friendRepository.save(any()) }
            assertTrue(user.generalAttributes.contains(attributeName))
            assertTrue(user.friends.first().attributes.containsKey(attributeName))
        }

        @Test
        fun `trying to add general attribute with incorrect name`() {
            val userId = 1L
            val attributeName = "N@me"
            val user = mockk<User>(relaxed = true)

            every { userRepository.findByIdOrNull(userId) } returns user

            assertThrows<InvalidAttributeNameException> {
                userService.addGeneralAttribute(userId, attributeName)
            }

            verify { userRepository.findById(userId) }
            verify(exactly = 0) { userRepository.save(user) }
            verify(exactly = 0) { friendRepository.save(any()) }
        }

        @Test
        fun `trying to add existing general attribute`() {
            val userId = 1L
            val attributeName = "Old Attribute"
            val user = mockk<User>(relaxed = true) {
                every { friends } returns mutableSetOf(
                    mockk(relaxed = true) {
                        every { attributes } returns mutableMapOf("Old Attribute" to "value")
                    }
                )
                every { generalAttributes } returns mutableSetOf("Old Attribute")
            }

            every { userRepository.findByIdOrNull(userId) } returns user

            assertThrows<AttributeAlreadyExistsException> {
                userService.addGeneralAttribute(userId, attributeName)
            }

            verify { userRepository.findByIdOrNull(userId) }
            verify(exactly = 0) { userRepository.save(user) }
            verify(exactly = 0) { friendRepository.save(any()) }
        }
    }

    @Nested
    inner class HasGeneralAttribute {
        @Test
        fun `user has general attribute`() {
            val userId = 1L
            val attributeName = "Old Attribute"
            val user = mockk<User>(relaxed = true) {
                every { generalAttributes } returns mutableSetOf(attributeName)
            }

            every { userRepository.findByIdOrNull(userId) } returns user

            val result = userService.hasGeneralAttribute(userId, attributeName)

            verify { userRepository.findById(userId) }
            assertTrue(result)
        }

        @Test
        fun `user does not have general attribute`() {
            val userId = 1L
            val attributeName = "New Attribute"
            val user = mockk<User>(relaxed = true) {
                every { generalAttributes } returns mutableSetOf("Old Attribute")
            }

            every { userRepository.findByIdOrNull(userId) } returns user

            val result = userService.hasGeneralAttribute(userId, attributeName)

            verify { userRepository.findById(userId) }
            assertFalse(result)
        }
    }

    @Nested
    inner class RemoveGeneralAttribute {
        @Test
        fun `correctly removing general attribute`() {
            val userId = 1L
            val attributeName = "attribute"
            val user = mockk<User>(relaxed = true) {
                every { generalAttributes } returns mutableSetOf(attributeName)
                every { friends } returns mutableSetOf(
                    mockk(relaxed = true) {
                        every { attributes } returns mutableMapOf(attributeName to "value")
                    }
                )
            }

            every { userRepository.findByIdOrNull(userId) } returns user
            every { userRepository.save(any()) } answers { arg(0) }
            every { friendRepository.save(any()) } answers { arg(0) }

            userService.removeGeneralAttribute(userId, attributeName)

            verify { userRepository.findByIdOrNull(userId) }
            verify { userRepository.save(user) }
            verify { friendRepository.save(any()) }
            assertFalse(user.generalAttributes.contains(attributeName))
            assertNull(user.friends.first().attributes[attributeName])
        }

        @Test
        fun `trying to remove non existent general attribute`() {
            val userId = 1L
            val user = User(userId, mutableSetOf(), mutableSetOf())

            every { userRepository.findByIdOrNull(userId) } returns user
            every { userRepository.save(any()) } answers { arg(0) }

            userRepository.save(user)

            assertThrows<AttributeDoesNotExistException> {
                userService.removeGeneralAttribute(userId, "attribute")
            }
        }
    }
}