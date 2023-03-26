package com.backend.kyf.service

import com.backend.kyf.dto.AttributeDTO
import com.backend.kyf.dto.FriendDTO
import com.backend.kyf.entity.Friend
import com.backend.kyf.entity.User
import com.backend.kyf.exception.*
import com.backend.kyf.repository.FriendRepository
import com.backend.kyf.repository.UserRepository
import com.backend.kyf.utils.mapper.FriendMapper
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import org.springframework.data.repository.findByIdOrNull


class FriendServiceTest {

    private val userRepository: UserRepository = mockk()
    private val friendRepository: FriendRepository = mockk()
    private val userService: UserService = mockk()
    private val friendMapper: FriendMapper = mockk()
    private val friendService: FriendService =
        FriendService(friendRepository, friendMapper, userRepository, userService)

    @BeforeEach
    fun setup() {
        clearAllMocks()
    }

    @Nested
    inner class AddFriend {
        @Test
        fun `adding friend successfully`() {
            val userId = 1L
            val friendId = 2L
            val friendName = "John"
            val friend = Friend(friendId, friendName, userId, mutableMapOf())
            val friendDTO = FriendDTO(friendId, friendName, userId, mutableMapOf())
            val user = User(userId, mutableSetOf(), mutableSetOf())

            every { userService.getUserById(userId) } returns user
            every { friendMapper.toEntity(friendDTO) } returns friend
            every { friendMapper.toDTO(friend) } returns friendDTO
            every { friendRepository.save(friend) } returns friend
            every { userRepository.save(user) } returns user

            friendService.addFriend(userId, friendDTO)

            assertThat(user.friends).contains(friend)
            verify { friendRepository.save(friend) }
            verify { userRepository.save(user) }
        }

        @Test
        fun `adding friend with incorrect name`() {
            val userId = 1L
            val friendDTO = FriendDTO(2L, "N@me", userId, mutableMapOf())

            every { userService.getUserById(userId) } returns User(userId, mutableSetOf(), mutableSetOf())

            assertThrows<InvalidFriendNameException> { friendService.addFriend(userId, friendDTO) }
        }

        @Test
        fun `adding friend with the name that already exists`() {
            val userId = 1L
            val friendId = 2L
            val friendName = "John"
            val friend = Friend(friendId, friendName, userId, mutableMapOf())
            val friendDTO = FriendDTO(friendId, friendName, userId, mutableMapOf())
            val user = User(userId, mutableSetOf(friend), mutableSetOf())

            every { userService.getUserById(userId) } returns user
            every { friendMapper.toEntity(friendDTO) } returns friend

            assertThrows<FriendAlreadyExistsException> { friendService.addFriend(userId, friendDTO) }
        }
    }

    @Nested
    inner class GetFriendById {
        @Test
        fun `correctly getting friend by id that belongs to the user`() {
            val userId = 1L
            val friendId = 2L
            val friend = Friend(friendId, "Johny", userId, mutableMapOf())

            every { friendRepository.findByIdOrNull(friendId) } returns friend

            val result = friendService.getFriendById(userId, friendId)

            assertThat(result).isEqualTo(friend)
        }

        @Test
        fun `getting non existing friend`() {
            val userId = 1L
            val friendId = 2L

            every { friendRepository.findByIdOrNull(friendId) } returns null

            assertThrows<FriendDoesNotExistException> { friendService.getFriendById(userId, friendId) }
        }

        @Test
        fun `getting a friend that does not belong to the user`() {
            val userId = 1L
            val friendId = 2L
            val friend = Friend(friendId, "John", 3L, mutableMapOf())

            every { friendRepository.findByIdOrNull(friendId) } returns friend

            assertThrows<AccessDeniedException> { friendService.getFriendById(userId, friendId) }
        }
    }

    @Nested
    inner class GetFriendDTOById {
        @Test
        fun `correctly getting friend dto from id`() {
            val userId = 1L
            val friendId = 2L
            val friendName = "John"
            val friend = Friend(friendId, friendName, userId, mutableMapOf())
            val friendDTO = FriendDTO(friendId, friendName, userId, mutableMapOf())

            every { friendRepository.findByIdOrNull(friendId) } returns friend
            every { friendMapper.toDTO(friend) } returns friendDTO

            val result = friendService.getFriendDTOById(userId, friendId)

            verify { friendMapper.toDTO(friend) }
            assertThat(result.id).isEqualTo(friendId)
            assertThat(result.name).isEqualTo(friendName)
            assertThat(result.ownerId).isEqualTo(userId)
            assertThat(result.attributes).isEqualTo(mutableMapOf<String, String?>())
        }
    }

    @Nested
    inner class GetFriendByName {
        @Test
        fun `correctly getting friend dto by name`() {
            val userId = 1L
            val friendId = 2L
            val friendName = "John"
            val friend = Friend(friendId, friendName, userId, mutableMapOf())
            val friendDTO = FriendDTO(friendId, friendName, userId, mutableMapOf())
            val user = User(userId, mutableSetOf(friend), mutableSetOf())

            every { userService.getUserById(userId) } returns user
            every { friendMapper.toDTO(friend) } returns friendDTO

            val result = friendService.getFriendByName(userId, friendName)

            verify { userService.getUserById(userId) }
            verify { friendMapper.toDTO(friend) }
            assertThat(result.id).isEqualTo(friendId)
            assertThat(result.name).isEqualTo(friendName)
            assertThat(result.ownerId).isEqualTo(userId)
            assertThat(result.attributes).isEqualTo(mutableMapOf<String, String?>())
        }

        @Test
        fun `trying to get friend by name that does not exist`() {
            val userId = 1L
            val friendName = "John"
            val user = User(userId, mutableSetOf(), mutableSetOf())

            every { userService.getUserById(userId) } returns user

            assertThrows<FriendDoesNotExistException> {
                friendService.getFriendByName(userId, friendName)
            }
        }
    }

    @Nested
    inner class GetAllFriends {
        @Test
        fun `getting all friends for a given user sorted by names`() {
            val userId = 1L
            val friend1 = Friend(2L, "Johny", userId, mutableMapOf())
            val friend1DTO = FriendDTO(2L, "Johny", userId, mutableMapOf())
            val friend2 = Friend(3L, "Bobby", userId, mutableMapOf())
            val friend2DTO = FriendDTO(3L, "Bobby", userId, mutableMapOf())
            val friends = mutableSetOf(friend1, friend2)
            val user = User(userId, friends, mutableSetOf())

            every { userService.getUserById(userId) } returns user
            every { friendMapper.toDTO(friend1) } returns friend1DTO
            every { friendMapper.toDTO(friend2) } returns friend2DTO

            val result = friendService.getAllFriends(userId)

            verify { userService.getUserById(userId) }
            verify { friendMapper.toDTO(friend1) }
            verify { friendMapper.toDTO(friend2) }
            assertThat(result).containsExactly(
                FriendDTO(3L, "Bobby", userId, mutableMapOf()),
                FriendDTO(2L, "Johny", userId, mutableMapOf())
            )
        }
    }

    @Nested
    inner class GetAllFriendsNames {
        @Test
        fun `getting sorted list of friend names for a given user`() {
            val userId = 1L
            val friend1 = Friend(2L, "Johny", userId, mutableMapOf())
            val friend2 = Friend(3L, "Bobby", userId, mutableMapOf())
            val friends = mutableSetOf(friend1, friend2)
            val user = User(userId, friends, mutableSetOf())

            every { userService.getUserById(userId) } returns user

            val result = friendService.getAllFriendNames(userId)

            verify { userService.getUserById(userId) }
            assertThat(result).containsExactly("Bobby", "Johny")
        }
    }

    @Nested
    inner class UpdateFriend {
        // TODO()
    }

    @Nested
    inner class ChangeFriendsName {
        @Test
        fun `correctly changing friend's name`() {
            val userId = 1L
            val friendId = 2L
            val newName = "Johny"
            val friend = Friend(friendId, "Billy", userId, mutableMapOf())
            val friendDTO = FriendDTO(friendId, "Billy", userId, mutableMapOf())
            val user = User(userId, mutableSetOf(friend), mutableSetOf())

            every { userService.getUserById(userId) } returns user
            every { friendRepository.findByIdOrNull(friendId) } returns friend
            every { friendRepository.save(friend.copy(name = newName)) } returns friend.copy(name = newName)
            every { friendMapper.toDTO(friend.copy(name = newName)) } returns friendDTO.copy(name = newName)

            val result = friendService.changeFriendsName(userId, friendId, newName)

            assertEquals(newName, result.name)
            verify { friendRepository.save(friend.copy(name = newName)) }
        }

        @Test
        fun `trying to change friend's name when provided name is not correct`() {
            val userId = 1L
            val friendId = 2L
            val newName = "I L0V3 CH@O$"
            val friend = Friend(friendId, "Billy", userId, mutableMapOf())
            val user = User(userId, mutableSetOf(friend), mutableSetOf())

            every { userService.getUserById(userId) } returns user
            every { friendRepository.findByIdOrNull(friendId) } returns friend

            assertThrows<InvalidFriendNameException> { friendService.changeFriendsName(userId, friendId, newName) }
            verify(exactly = 0) { friendRepository.save(any()) }
        }

        @Test
        fun `trying to change friend's name to a name that already exists`() {
            val userId = 1L
            val modifiedFriendId = 2L
            val newName = "Johny"
            val friend1 = Friend(modifiedFriendId, "Billy", userId, mutableMapOf())
            val friend2 = Friend(3L, "Johny", userId, mutableMapOf())
            val user = User(userId, mutableSetOf(friend1, friend2), mutableSetOf())

            every { userService.getUserById(userId) } returns user
            every { friendRepository.findByIdOrNull(modifiedFriendId) } returns friend1

            assertThrows<FriendAlreadyExistsException> {
                friendService.changeFriendsName(
                    userId,
                    modifiedFriendId,
                    newName
                )
            }
            verify(exactly = 0) { friendRepository.save(any()) }
        }
    }

    @Nested
    inner class RemoveFriend {
        @Test
        fun `correctly removing friend`() {
            val userId = 1L
            val friendId = 2L
            val friend = Friend(friendId, "Billy", userId, mutableMapOf())
            val user = User(userId, mutableSetOf(friend), mutableSetOf())

            every { userService.getUserById(userId) } returns user
            every { friendRepository.findByIdOrNull(friendId) } returns friend
            every { friendRepository.deleteById(friendId) } just runs

            friendService.removeFriend(userId, friendId)

            verify { friendRepository.deleteById(friendId) }
            verify { userService.getUserById(userId) }
            assertFalse(userService.getUserById(userId).friends.contains(friend))
        }

        @Test
        fun `trying to remove a friend without access`() {
            val userId = 1L
            val friendId = 2L
            val friend = Friend(friendId, "Billy", 3L, mutableMapOf())
            val user = User(userId, mutableSetOf(friend), mutableSetOf())

            every { userService.getUserById(userId) } returns user
            every { friendRepository.findByIdOrNull(friendId) } returns friend

            assertThrows<AccessDeniedException> {
                friendService.removeFriend(userId, friendId)
            }
        }

        @Test
        fun `trying to remove a friend which does not exist`() {
            val userId = 1L
            val friendId = 2L
            val user = User(userId, mutableSetOf(), mutableSetOf())

            every { userService.getUserById(userId) } returns user
            every { friendRepository.findByIdOrNull(friendId) } returns null

            assertThrows<FriendDoesNotExistException> {
                friendService.removeFriend(userId, friendId)
            }
        }
    }

    @Nested
    inner class AddAttribute {
        @Test
        fun `correctly adding new attribute to a friend`() {
            val userId = 1L
            val friendId = 2L
            val attributeDTO = AttributeDTO("Age", "21")
            val friend = Friend(friendId, "John", userId, mutableMapOf())
            val friendDTO = FriendDTO(friendId, "John", userId, mutableMapOf())
            val user = User(userId, mutableSetOf(friend), mutableSetOf())

            every { userService.getUserById(userId) } returns user
            every { friendRepository.findByIdOrNull(friendId) } returns friend
            every { friendRepository.save(any()) } returns friend.copy(attributes = mutableMapOf(attributeDTO.name to attributeDTO.value))
            every { friendMapper.toDTO(any()) } returns friendDTO.copy(attributes = mutableMapOf(attributeDTO.name to attributeDTO.value))

            val result = friendService.addAttribute(userId, friendId, attributeDTO)

            assertEquals(attributeDTO.name, result.attributes.keys.first())
            assertEquals(attributeDTO.value, result.attributes.values.first())
            verify { friendRepository.save(friend.copy(attributes = mutableMapOf(attributeDTO.name to attributeDTO.value))) }
        }

        @Test
        fun `trying to add attribute with invalid name`() {
            val userId = 1L
            val friendId = 2L
            val attributeName = "Age?"
            val attributeValue = "21"
            val friend = Friend(friendId, "John", userId, mutableMapOf())
            val user = User(userId, mutableSetOf(friend), mutableSetOf())

            every { userService.getUserById(userId) } returns user
            every { friendRepository.findByIdOrNull(friendId) } returns friend

            assertThrows<InvalidAttributeNameException> {
                friendService.addAttribute(userId, friendId, AttributeDTO(attributeName, attributeValue))
            }
            verify(exactly = 0) { friendRepository.save(any()) }
        }

        @Test
        fun `trying to add new attribute when attribute with the same name already exists`() {
            val userId = 1L
            val friendId = 2L
            val attributeName = "Age"
            val attributeValue = "21"
            val friend = Friend(friendId, "John", userId, mutableMapOf(attributeName to attributeValue))
            val user = User(userId, mutableSetOf(friend), mutableSetOf())

            every { userService.getUserById(userId) } returns user
            every { friendRepository.findByIdOrNull(friendId) } returns friend

            assertThrows<AttributeAlreadyExistsException> {
                friendService.addAttribute(userId, friendId, AttributeDTO(attributeName, "22"))
            }
            verify(exactly = 0) { friendRepository.save(any()) }
        }
    }

    @Nested
    inner class HasAttribute {
        @Test
        fun `return true if friend has the given attribute`() {
            val userId = 1L
            val friendId = 2L
            val attributeName = "age"
            val friend = Friend(friendId, "John", userId, mutableMapOf(attributeName to "25"))
            val user = User(userId, mutableSetOf(friend), mutableSetOf())

            every { userService.getUserById(userId) } returns user
            every { friendRepository.findByIdOrNull(friendId) } returns friend

            val result = friendService.hasAttribute(userId, friendId, attributeName)

            assertTrue(result)
        }

        @Test
        fun `return false if friend does not have given attribute`() {
            val userId = 1L
            val friendId = 2L
            val attributeName = "age"
            val friend = Friend(friendId, "John", userId, mutableMapOf("name" to "John"))
            val user = User(userId, mutableSetOf(friend), mutableSetOf())

            every { userService.getUserById(userId) } returns user
            every { friendRepository.findByIdOrNull(friendId) } returns friend

            val result = friendService.hasAttribute(userId, friendId, attributeName)

            assertFalse(result)
        }
    }

    @Nested
    inner class GetAttributes {
        @Test
        fun `correctly returning friend's attributes`() {
            val userId = 1L
            val friendId = 2L
            val attributes = mutableMapOf<String, String?>("Age" to "21", "Favorite artist" to "The Beatles")
            val friend = Friend(friendId, "John", userId, attributes)

            every { friendRepository.findByIdOrNull(friendId) } returns friend

            val result = friendService.getAttributes(userId, friendId)

            assertEquals(2, result.size)
            assertTrue(result.contains(AttributeDTO("Age", "21")))
            assertTrue(result.contains(AttributeDTO("Favorite artist", "The Beatles")))
        }

        @Test
        fun `returning empty list when friend has no attributes`() {
            val userId = 1L
            val friendId = 2L
            val friend = Friend(friendId, "John", userId, mutableMapOf())

            every { friendRepository.findByIdOrNull(friendId) } returns friend

            val result = friendService.getAttributes(userId, friendId)

            assertTrue(result.isEmpty())
        }
    }

    @Nested
    inner class GetAttributeNames {
        @Test
        fun `getting friend's attribute names`() {
            val userId = 1L
            val friendId = 2L
            val friend = Friend(friendId, "John", userId, mutableMapOf("Age" to "21", "City" to "London"))
            val user = User(userId, mutableSetOf(friend), mutableSetOf())

            every { userService.getUserById(userId) } returns user
            every { friendRepository.findByIdOrNull(friendId) } returns friend

            val result = friendService.getAttributeNames(userId, friendId)

            assertEquals(listOf("Age", "City"), result)
        }

        @Test
        fun `returning empty list if friend has no attributes`() {
            val userId = 1L
            val friendId = 2L
            val friend = Friend(friendId, "John", userId, mutableMapOf())
            val user = User(userId, mutableSetOf(friend), mutableSetOf())

            every { userService.getUserById(userId) } returns user
            every { friendRepository.findByIdOrNull(friendId) } returns friend

            val result = friendService.getAttributeNames(userId, friendId)

            assertTrue(result.isEmpty())
        }
    }

    @Nested
    inner class UpdateAttribute {
        @Test
        fun `correctly updating attribute value`() {
            val userId = 1L
            val friendId = 2L
            val attributeName = "Age"
            val attributeValue = "20"
            val newAttributeValue = "21"
            val friend = Friend(friendId, "John", userId, mutableMapOf(attributeName to attributeValue))
            val friendDTO = FriendDTO(friendId, "John", userId, mutableMapOf(attributeName to attributeValue))
            val user = User(userId, mutableSetOf(friend), mutableSetOf())

            every { userService.getUserById(userId) } returns user
            every { friendRepository.findByIdOrNull(friendId) } returns friend
            every { friendRepository.save(any()) } returns friend.copy(attributes = mutableMapOf(attributeName to newAttributeValue))
            every { friendMapper.toDTO(any()) } returns friendDTO.copy(attributes = mutableMapOf(attributeName to newAttributeValue))

            val result = friendService.updateAttribute(userId, friendId, AttributeDTO(attributeName, newAttributeValue))

            assertEquals(newAttributeValue, result.attributes[attributeName])
            verify { friendRepository.save(friend.copy(attributes = mutableMapOf(attributeName to newAttributeValue))) }
        }

        @Test
        fun `trying to update an attribute which does not exist`() {
            val userId = 1L
            val friendId = 2L
            val attributeName = "Age"
            val attributeValue = "21"
            val friend = Friend(friendId, "John", userId, mutableMapOf(attributeName to attributeValue))
            val user = User(userId, mutableSetOf(friend), mutableSetOf())

            every { userService.getUserById(userId) } returns user
            every { friendRepository.findByIdOrNull(friendId) } returns friend

            assertThrows<AttributeDoesNotExistException> {
                friendService.updateAttribute(userId, friendId, AttributeDTO("Favorite test color", "Green"))
            }

            verify(exactly = 0) { friendRepository.save(any()) }
        }
    }

    @Nested
    inner class RemoveAttribute {
        @Test
        fun `correctly removing attribute from friend`() {
            val userId = 1L
            val friendId = 2L
            val attributeName = "Age"
            val friend = Friend(friendId, "John", userId, mutableMapOf(attributeName to "21", "City" to "London"))
            val friendDTO = FriendDTO(friendId, "John", userId, mutableMapOf(attributeName to "21", "City" to "London"))
            val user = User(userId, mutableSetOf(friend), mutableSetOf())

            every { userService.getUserById(userId) } returns user
            every { friendRepository.findByIdOrNull(friendId) } returns friend
            every { friendRepository.save(any()) } returns friend.copy(attributes = mutableMapOf("City" to "London"))
            every { friendMapper.toDTO(any()) } returns friendDTO.copy(attributes = mutableMapOf("City" to "London"))

            val result = friendService.removeAttribute(userId, friendId, attributeName)

            assertEquals(1, result.attributes.size)
            verify { friendRepository.save(friend.copy(attributes = mutableMapOf("City" to "London"))) }
        }
    }
}