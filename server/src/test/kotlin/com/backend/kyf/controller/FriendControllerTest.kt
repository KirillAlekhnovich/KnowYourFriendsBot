package com.backend.kyf.controller

import com.backend.kyf.dto.AttributeDTO
import com.backend.kyf.dto.FriendDTO
import com.backend.kyf.entity.Friend
import com.backend.kyf.entity.User
import com.backend.kyf.exception.*
import com.backend.kyf.repository.FriendRepository
import com.backend.kyf.service.FriendService
import com.backend.kyf.service.UserService
import com.backend.kyf.utils.ResponseBuilder.buildResponse
import com.backend.kyf.utils.auth.AuthService
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.*


@SpringBootTest
@AutoConfigureMockMvc
class FriendControllerTest(@Autowired val mockMvc: MockMvc) {

    @MockkBean
    private lateinit var friendService: FriendService

    @MockkBean
    private lateinit var authService: AuthService

    @MockkBean
    private lateinit var userService: UserService

    @MockkBean
    private lateinit var friendRepository: FriendRepository

    @BeforeEach
    fun setup() {
        clearAllMocks()
    }

    @Nested
    inner class AddFriend {
        @Test
        fun `correctly adding friend`() {
            val userId = 1L
            val accessToken = "valid-access-token"
            val friendDTO = FriendDTO(2L, "Johny", userId, mutableMapOf())

            every { authService.authorizeUser(accessToken) } returns userId
            every { authService.getAuthorizedUserId() } returns userId
            every { friendService.addFriend(userId, friendDTO) } returns friendDTO

            mockMvc.post("/friends/add") {
                contentType = MediaType.APPLICATION_JSON
                accept = MediaType.APPLICATION_JSON
                header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
                content = asJsonString(friendDTO)
            }.andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content {
                    json(
                        """
                    {
                        "message": "Friend added",
                        "data": {
                            "id": 2,
                            "name": "Johny",
                            "ownerId": 1,
                            "attributes": {}
                        }
                    }
                    """.trimIndent()
                    )
                }
            }

            verify { authService.authorizeUser(accessToken) }
            verify { friendService.addFriend(userId, friendDTO) }
        }

        @Test
        fun `trying to add friend by unauthorized user`() {
            val userId = 1L
            val friendDTO = FriendDTO(2L, "Johny", userId, mutableMapOf())

            mockMvc.post("/friends/add") {
                contentType = MediaType.APPLICATION_JSON
                accept = MediaType.APPLICATION_JSON
                content = asJsonString(friendDTO)
            }.andExpect {
                status { isForbidden() }
            }
        }

        @Test
        fun `trying to add friend for another user`() {
            val userId = 1L
            val accessToken = "invalid-access-token"
            val friendDTO = FriendDTO(2L, "Johny", userId, mutableMapOf())

            every { authService.authorizeUser(accessToken) } throws AccessDeniedException()

            assertThrows<AccessDeniedException> {
                mockMvc.post("/friends/add") {
                    contentType = MediaType.APPLICATION_JSON
                    accept = MediaType.APPLICATION_JSON
                    header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
                    content = asJsonString(friendDTO)
                }
            }.also {
                assertThat(it.message).isEqualTo("You have no access to this data")
            }

            verify(exactly = 0) { friendService.addFriend(any(), any()) }
        }


        @Test
        fun `trying to add a friend that already exists`() {
            val userId = 1L
            val accessToken = "valid-access-token"
            val friendDTO = FriendDTO(2L, "Johny", userId, mutableMapOf())

            every { authService.authorizeUser(accessToken) } returns userId
            every { authService.getAuthorizedUserId() } returns userId
            every { friendService.addFriend(userId, friendDTO) } throws FriendAlreadyExistsException()

            mockMvc.post("/friends/add") {
                contentType = MediaType.APPLICATION_JSON
                accept = MediaType.APPLICATION_JSON
                header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
                content = asJsonString(friendDTO)
            }.andExpect {
                status { isConflict() }
            }

            verify { authService.authorizeUser(accessToken) }
            verify { authService.getAuthorizedUserId() }
            verify { friendService.addFriend(userId, friendDTO) }
        }

        @Test
        fun `trying to add friend with an invalid name`() {
            val userId = 1L
            val accessToken = "valid-access-token"
            val friendDTO = FriendDTO(2L, "12#", userId, mutableMapOf())

            every { authService.authorizeUser(accessToken) } returns userId
            every { authService.getAuthorizedUserId() } returns userId
            every { friendService.addFriend(userId, friendDTO) } throws InvalidFriendNameException()

            mockMvc.post("/friends/add") {
                contentType = MediaType.APPLICATION_JSON
                accept = MediaType.APPLICATION_JSON
                header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
                content = asJsonString(friendDTO)
            }.andExpect {
                status { isNotAcceptable() }
            }

            verify { authService.authorizeUser(accessToken) }
            verify { friendService.addFriend(userId, friendDTO) }
        }
    }

    @Nested
    inner class GetFriend {
        @Test
        fun `correctly getting friend by id`() {
            val userId = 1L
            val friendId = 2L
            val accessToken = "valid-access-token"
            val friendDTO = FriendDTO(2L, "Johny", userId, mutableMapOf())

            every { authService.getAuthorizedUserId() } returns userId
            every { authService.authorizeUser(accessToken) } returns userId
            every { friendService.getFriendDTOById(userId, friendId) } returns friendDTO

            mockMvc.get("/friends/$friendId") {
                accept = MediaType.APPLICATION_JSON
                header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            }.andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content {
                    json(
                        """
                {
                    "message": "Friend retrieved",
                    "data": {
                        "id": 2,
                        "name": "Johny",
                        "ownerId": 1,
                        "attributes": {}
                    }
                }
                """.trimIndent()
                    )
                }
            }

            verify { authService.authorizeUser(accessToken) }
            verify { friendService.getFriendDTOById(userId, friendId) }
        }

        @Test
        fun `trying to get friend by unauthorized user`() {
            val friendId = 2L

            mockMvc.get("/friends/$friendId") {
                contentType = MediaType.APPLICATION_JSON
                accept = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isForbidden() }
            }
        }

        @Test
        fun `trying to get a friend of another user`() {
            val userId = 1L
            val friendId = 2L
            val accessToken = "invalid-access-token"

            every { authService.getAuthorizedUserId() } returns userId
            every { authService.authorizeUser(accessToken) } returns userId
            every { friendService.getFriendDTOById(userId, friendId) } throws AccessDeniedException()

            mockMvc.get("/friends/$friendId") {
                accept = MediaType.APPLICATION_JSON
                header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            }.andExpect {
                status { isUnauthorized() }
            }

            verify { authService.authorizeUser(accessToken) }
            verify { friendService.getFriendDTOById(userId, friendId) }
        }

        @Test
        fun `trying to get a friend that does not exist`() {
            val userId = 1L
            val friendId = 2L
            val accessToken = "valid-access-token"

            every { authService.getAuthorizedUserId() } returns userId
            every { authService.authorizeUser(accessToken) } returns userId
            every { friendService.getFriendDTOById(userId, friendId) } throws FriendDoesNotExistException()

            mockMvc.get("/friends/$friendId") {
                accept = MediaType.APPLICATION_JSON
                header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            }.andExpect {
                status { isNotFound() }
            }

            verify { authService.authorizeUser(accessToken) }
            verify { friendService.getFriendDTOById(userId, friendId) }
        }

    }

    @Nested
    inner class GetFriendByName {
        @Test
        fun `correctly getting a friend by name`() {
            val userId = 1L
            val accessToken = "valid-access-token"
            val friendName = "John"
            val friendDTO = FriendDTO(2L, friendName, userId, mutableMapOf())

            every { authService.authorizeUser(accessToken) } returns userId
            every { authService.getAuthorizedUserId() } returns userId
            every { friendService.getFriendByName(userId, friendName) } returns friendDTO

            mockMvc.get("/friends/search/$friendName") {
                contentType = MediaType.APPLICATION_JSON
                accept = MediaType.APPLICATION_JSON
                header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            }.andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content {
                    json(
                        """
                {
                    "message": "Friend retrieved",
                    "data": {
                        "id": 2,
                        "name": "John",
                        "ownerId": 1,
                        "attributes": {}
                    }
                }
                """.trimIndent()
                    )
                }
            }

            verify { authService.authorizeUser(accessToken) }
            verify { friendService.getFriendByName(userId, friendName) }
        }

        @Test
        fun `trying to get a friend by name which does not exist`() {
            val userId = 1L
            val accessToken = "valid-access-token"
            val friendName = "John"

            every { authService.authorizeUser(accessToken) } returns userId
            every { authService.getAuthorizedUserId() } returns userId
            every { friendService.getFriendByName(userId, friendName) } throws FriendDoesNotExistException()

            mockMvc.get("/friends/search/$friendName") {
                contentType = MediaType.APPLICATION_JSON
                accept = MediaType.APPLICATION_JSON
                header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            }.andExpect {
                status { isNotFound() }
            }

            verify { authService.authorizeUser(accessToken) }
            verify { friendService.getFriendByName(userId, friendName) }
        }

        @Test
        fun `trying to get friend by name by unauthorized user`() {
            val friendName = "John"

            mockMvc.get("/friends/search/$friendName") {
                contentType = MediaType.APPLICATION_JSON
                accept = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isForbidden() }
            }
        }

        @Test
        fun `trying to get someone else's friend by name`() {
            val accessToken = "invalid-access-token"
            val friendName = "John"

            every { authService.authorizeUser(accessToken) } throws AccessDeniedException()

            assertThrows<AccessDeniedException> {
                mockMvc.get("/friends/search/$friendName") {
                    contentType = MediaType.APPLICATION_JSON
                    accept = MediaType.APPLICATION_JSON
                    header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
                }
            }.also {
                assertThat(it.message).isEqualTo("You have no access to this data")
            }

            verify(exactly = 0) { friendService.addFriend(any(), any()) }
        }
    }

    @Nested
    inner class GetAllFriends {
        @Test
        fun `correctly getting all friends`() {
            val userId = 1L
            val accessToken = "valid-access-token"

            val friend1 = FriendDTO(1L, "John", userId, mutableMapOf())
            val friend2 = FriendDTO(2L, "Alice", userId, mutableMapOf())
            val friend3 = FriendDTO(3L, "Bob", userId, mutableMapOf())

            every { authService.getAuthorizedUserId() } returns userId
            every { authService.authorizeUser(accessToken) } returns userId
            every { friendService.getAllFriends(userId) } returns listOf(friend1, friend2, friend3)

            mockMvc.get("/friends") {
                contentType = MediaType.APPLICATION_JSON
                accept = MediaType.APPLICATION_JSON
                header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            }.andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content {
                    json(
                        """
                {
                    "message": "List of friends retrieved",
                    "data": [
                        {"id": 2, "name": "Alice", "ownerId": 1, "attributes": {}},
                        {"id": 3, "name": "Bob", "ownerId": 1, "attributes": {}},
                        {"id": 1, "name": "John", "ownerId": 1, "attributes": {}}
                    ]
                }
                """.trimIndent()
                    )
                }
            }

            verify { authService.authorizeUser(accessToken) }
            verify { friendService.getAllFriends(userId) }
        }

        @Test
        fun `correctly getting an empty list of friends`() {
            val userId = 1L
            val accessToken = "valid-access-token"

            every { authService.getAuthorizedUserId() } returns userId
            every { authService.authorizeUser(accessToken) } returns userId
            every { friendService.getAllFriends(userId) } returns emptyList()

            mockMvc.get("/friends") {
                contentType = MediaType.APPLICATION_JSON
                accept = MediaType.APPLICATION_JSON
                header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            }.andExpect {
                status { isOk() }
                content { json("{'message':'List of friends retrieved','data':[]}") }
            }

            verify(exactly = 1) { authService.authorizeUser(accessToken) }
            verify(exactly = 1) { friendService.getAllFriends(userId) }
        }

        @Test
        fun `trying to get all friends by unauthorized user`() {
            mockMvc.get("/friends") {
                contentType = MediaType.APPLICATION_JSON
                accept = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isForbidden() }
            }
        }

        @Test
        fun `trying to get all someone else's friends`() {
            val accessToken = "invalid-access-token"

            every { authService.authorizeUser(accessToken) } throws AccessDeniedException()

            assertThrows<AccessDeniedException> {
                mockMvc.get("/friends") {
                    contentType = MediaType.APPLICATION_JSON
                    accept = MediaType.APPLICATION_JSON
                    header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
                }
            }.also {
                assertThat(it.message).isEqualTo("You have no access to this data")
            }

            verify(exactly = 0) { friendService.addFriend(any(), any()) }
        }
    }

    @Nested
    inner class GetAllFriendNames {
        @Test
        fun `correctly getting all friend names`() {
            val userId = 1L
            val accessToken = "valid-access-token"
            val friends = listOf(
                Friend(1L, "John", userId, mutableMapOf()),
                Friend(2L, "Alice", userId, mutableMapOf()),
                Friend(3L, "Bob", userId, mutableMapOf())
            )

            every { authService.getAuthorizedUserId() } returns userId
            every { authService.authorizeUser(accessToken) } returns userId
            every { friendService.getAllFriendNames(userId) } returns friends.map { it.name }.sorted()

            mockMvc.get("/friends/names") {
                contentType = MediaType.APPLICATION_JSON
                accept = MediaType.APPLICATION_JSON
                header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            }.andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content {
                    json(
                        """
                        {
                            "message": "List of friend names retrieved",
                            "data": ["Alice", "Bob", "John"]
                        }"""
                    )
                }
            }

            verify(exactly = 1) { authService.authorizeUser(accessToken) }
            verify(exactly = 1) { friendService.getAllFriendNames(userId) }
        }

        @Test
        fun `trying to get all friends names while having no friends`() {
            val userId = 1L
            val accessToken = "valid-access-token"

            every { authService.getAuthorizedUserId() } returns userId
            every { authService.authorizeUser(accessToken) } returns userId
            every { friendService.getAllFriendNames(userId) } returns emptyList()

            mockMvc.get("/friends/names") {
                contentType = MediaType.APPLICATION_JSON
                accept = MediaType.APPLICATION_JSON
                header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            }.andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content { json("{'message':'List of friend names retrieved','data':[]}") }
            }

            verify(exactly = 1) { authService.authorizeUser(accessToken) }
            verify(exactly = 1) { friendService.getAllFriendNames(userId) }
        }

        @Test
        fun `trying to get all friends names by unauthorized user`() {
            mockMvc.get("/friends/names") {
                contentType = MediaType.APPLICATION_JSON
                accept = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isForbidden() }
            }
        }

        @Test
        fun `trying to get all friends names that belong to someone else`() {
            val accessToken = "invalid-access-token"

            every { authService.authorizeUser(accessToken) } throws AccessDeniedException()

            assertThrows<AccessDeniedException> {
                mockMvc.get("/friends/names") {
                    contentType = MediaType.APPLICATION_JSON
                    accept = MediaType.APPLICATION_JSON
                    header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
                }
            }.also {
                assertThat(it.message).isEqualTo("You have no access to this data")
            }

            verify(exactly = 0) { friendService.addFriend(any(), any()) }
        }
    }

    @Nested
    inner class UpdateFriend {
        // TODO
    }

    @Nested
    inner class ChangeFriendsName {
        @Test
        fun `correctly changing friend's name`() {
            val userId = 1L
            val friendId = 2L
            val accessToken = "valid-access-token"
            val newName = "Alice"

            every { authService.getAuthorizedUserId() } returns userId
            every { authService.authorizeUser(accessToken) } returns userId
            every { friendService.changeFriendsName(userId, friendId, newName) } returns FriendDTO(
                friendId,
                newName,
                userId,
                mutableMapOf()
            )

            val result = mockMvc.put("/friends/$friendId/change_name") {
                contentType = MediaType.APPLICATION_JSON
                accept = MediaType.APPLICATION_JSON
                header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
                content = newName
            }.andExpect {
                status { isOk() }
                content {
                    json(
                        """
                {
                    "message": "Friend's name has been changed to $newName",
                    "data": {
                        "id": $friendId,
                        "name": "$newName",
                        "ownerId": $userId,
                        "attributes": {}
                    }
                }
                """
                    )
                }
            }.andReturn()

            val expectedResponse = asJsonString(
                buildResponse(
                    "Friend's name has been changed to $newName",
                    FriendDTO(friendId, newName, userId, mutableMapOf())
                )
            )
            val actualResponse = result.response.contentAsString

            assertEquals(expectedResponse, actualResponse)
        }

        @Test
        fun `trying to change friend's name to already existing one`() {
            val userId = 1L
            val friendId = 2L
            val accessToken = "valid-access-token"
            val newName = "Bob"

            every { authService.getAuthorizedUserId() } returns userId
            every { authService.authorizeUser(accessToken) } returns userId
            every { friendService.changeFriendsName(userId, friendId, newName) } throws FriendAlreadyExistsException()

            mockMvc.put("/friends/$friendId/change_name") {
                contentType = MediaType.APPLICATION_JSON
                accept = MediaType.APPLICATION_JSON
                header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
                content = newName
            }.andExpect {
                status { isConflict() }
                content {
                    json("""{"message": "Friend already exists"}""")
                }
            }
        }

        @Test
        fun `trying to change friend's name which does not exist`() {
            val userId = 1L
            val friendId = 2L
            val accessToken = "valid-access-token"
            val newName = "Bob"

            every { authService.getAuthorizedUserId() } returns userId
            every { authService.authorizeUser(accessToken) } returns userId
            every { friendService.changeFriendsName(userId, friendId, newName) } throws FriendDoesNotExistException()

            mockMvc.put("/friends/$friendId/change_name") {
                contentType = MediaType.APPLICATION_JSON
                accept = MediaType.APPLICATION_JSON
                header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
                content = newName
            }.andExpect {
                status { isNotFound() }
                content {
                    json("""{"message": "Friend does not exist"}""")
                }
            }
        }

        @Test
        fun `trying to change friend's name to an invalid name`() {
            val userId = 1L
            val friendId = 2L
            val accessToken = "valid-access-token"
            val newName = "N@me"

            every { authService.getAuthorizedUserId() } returns userId
            every { authService.authorizeUser(accessToken) } returns userId
            every { friendService.changeFriendsName(userId, friendId, newName) } throws InvalidFriendNameException()

            mockMvc.put("/friends/$friendId/change_name") {
                contentType = MediaType.APPLICATION_JSON
                accept = MediaType.APPLICATION_JSON
                header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
                content = newName
            }.andExpect {
                status { isNotAcceptable() }
                content {
                    json("""{"message": "Friend name is invalid"}""")
                }
            }
        }

        @Test
        fun `trying to change friend's name by unauthorized user`() {
            val friendId = 1L
            val newName = "Billy"

            mockMvc.put("/friends/$friendId/change_name") {
                contentType = MediaType.APPLICATION_JSON
                accept = MediaType.APPLICATION_JSON
                content = newName
            }.andExpect {
                status { isForbidden() }
            }
        }

        @Test
        fun `trying to change friend's name that does not belong to a user`() {
            val friendId = 1L
            val accessToken = "invalid-access-token"
            val newName = "Billy"

            every { authService.authorizeUser(accessToken) } throws AccessDeniedException()

            assertThrows<AccessDeniedException> {
                mockMvc.put("/friends/$friendId/change_name") {
                    contentType = MediaType.APPLICATION_JSON
                    accept = MediaType.APPLICATION_JSON
                    header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
                    content = newName
                }
            }.also {
                assertThat(it.message).isEqualTo("You have no access to this data")
            }

            verify(exactly = 0) { friendService.addFriend(any(), any()) }
        }
    }

    @Nested
    inner class RemoveFriend {
        @Test
        fun `correctly removing friend`() {
            val userId = 1L
            val friendId = 2L
            val accessToken = "valid-access-token"
            val friend = Friend(2L, "Johny", userId, mutableMapOf())

            every { authService.getAuthorizedUserId() } returns userId
            every { authService.authorizeUser(accessToken) } returns userId
            every { friendRepository.findByIdOrNull(friendId) } returns friend
            every { userService.getUserById(userId) } returns User(userId, mutableSetOf(friend), mutableSetOf())
            every { friendService.removeFriend(userId, friendId) } just runs

            mockMvc.delete("/friends/$friendId/remove") {
                header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            }.andExpect {
                status { isOk() }
                content { json("""{"message":"Friend removed","data":{}}}""") }
            }

            verify(exactly = 1) { authService.getAuthorizedUserId() }
            verify(exactly = 1) { authService.authorizeUser(accessToken) }
            verify(exactly = 1) { friendService.removeFriend(userId, friendId) }
        }

        @Test
        fun `trying to remove a friend that does not exist`() {
            val userId = 1L
            val friendId = 2L
            val accessToken = "valid-access-token"

            every { authService.getAuthorizedUserId() } returns userId
            every { authService.authorizeUser(accessToken) } returns userId
            every { friendService.removeFriend(userId, friendId) } throws FriendDoesNotExistException()

            mockMvc.delete("/friends/$friendId/remove") {
                header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            }.andExpect {
                status { isNotFound() }
                content { json("""{"message":"Friend does not exist"}""") }
            }

            verify { friendService.removeFriend(userId, friendId) }
        }

        @Test
        fun `trying to remove a friend by unauthorized user`() {
            val friendId = 1L

            mockMvc.delete("/friends/$friendId/remove") {
                contentType = MediaType.APPLICATION_JSON
                accept = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isForbidden() }
            }
        }

        @Test
        fun `trying to remove a friend that does not belong to authorized user`() {
            val friendId = 1L
            val accessToken = "invalid-access-token"

            every { authService.authorizeUser(accessToken) } throws AccessDeniedException()

            assertThrows<AccessDeniedException> {
                mockMvc.delete("/friends/$friendId/remove") {
                    contentType = MediaType.APPLICATION_JSON
                    accept = MediaType.APPLICATION_JSON
                    header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
                }
            }.also {
                assertThat(it.message).isEqualTo("You have no access to this data")
            }

            verify(exactly = 0) { friendService.addFriend(any(), any()) }
        }
    }

    @Nested
    inner class AddAttribute {
        @Test
        fun `correctly adding an attribute to a friend`() {
            val userId = 1L
            val friendId = 2L
            val accessToken = "valid-access-token"
            val friendDTO = FriendDTO(friendId, "John", userId, mutableMapOf())
            val attribute = AttributeDTO("Age", "25")
            val jsonRequest = asJsonString(attribute)

            every { authService.getAuthorizedUserId() } returns userId
            every { authService.authorizeUser(accessToken) } returns userId
            every {
                friendService.addAttribute(
                    userId,
                    friendId,
                    attribute
                )
            } returns friendDTO.copy(attributes = mutableMapOf(attribute.name to attribute.value))

            mockMvc.put("/friends/${friendDTO.id}/add_attribute") {
                contentType = MediaType.APPLICATION_JSON
                content = jsonRequest
                header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            }.andExpect {
                status { isOk() }
                content {
                    json(
                        """
                {
                    "message": "Attribute added",
                    "data": {
                        "id": 2,
                        "name": "John",
                        "ownerId": 1,
                        "attributes": {"${attribute.name}": "${attribute.value}"}
                    }
                }
                """
                    )
                }
            }
        }

        @Test
        fun `trying to add an attribute which already exists`() {
            val userId = 1L
            val friendId = 2L
            val accessToken = "valid-access-token"

            every { authService.getAuthorizedUserId() } returns userId
            every { authService.authorizeUser(accessToken) } returns userId

            val existingAttribute = AttributeDTO("Age", "25")
            val jsonRequest = asJsonString(existingAttribute)

            every {
                friendService.addAttribute(
                    userId,
                    friendId,
                    existingAttribute
                )
            } throws AttributeAlreadyExistsException()

            mockMvc.put("/friends/$friendId/add_attribute") {
                contentType = MediaType.APPLICATION_JSON
                content = jsonRequest
                header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            }.andExpect {
                status { isConflict() }
                content {
                    json("""{"message": "Attribute already exists"}""")
                }
            }
        }

        @Test
        fun `trying to add an attribute with invalid name`() {
            val userId = 1L
            val friendId = 2L
            val accessToken = "valid-access-token"

            every { authService.getAuthorizedUserId() } returns userId
            every { authService.authorizeUser(accessToken) } returns userId

            val existingAttribute = AttributeDTO("@ge", "25")
            val jsonRequest = asJsonString(existingAttribute)

            every {
                friendService.addAttribute(
                    userId,
                    friendId,
                    existingAttribute
                )
            } throws InvalidAttributeNameException()

            mockMvc.put("/friends/$friendId/add_attribute") {
                contentType = MediaType.APPLICATION_JSON
                content = jsonRequest
                header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            }.andExpect {
                status { isNotAcceptable() }
                content {
                    json("""{"message": "Attribute name is invalid"}""")
                }
            }
        }

        @Test
        fun `trying to add an attribute by unauthorized user`() {
            val friendId = 1L
            val existingAttribute = AttributeDTO("Age", "25")
            val jsonRequest = asJsonString(existingAttribute)

            mockMvc.put("/friends/$friendId/add_attribute") {
                contentType = MediaType.APPLICATION_JSON
                accept = MediaType.APPLICATION_JSON
                content = jsonRequest
            }.andExpect {
                status { isForbidden() }
            }
        }

        @Test
        fun `trying to add an attribute to a friend which does not belong to authorized user`() {
            val friendId = 1L
            val accessToken = "invalid-access-token"
            val existingAttribute = AttributeDTO("Age", "25")
            val jsonRequest = asJsonString(existingAttribute)

            every { authService.authorizeUser(accessToken) } throws AccessDeniedException()

            assertThrows<AccessDeniedException> {
                mockMvc.put("/friends/$friendId/add_attribute") {
                    contentType = MediaType.APPLICATION_JSON
                    accept = MediaType.APPLICATION_JSON
                    header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
                    content = jsonRequest
                }
            }.also {
                assertThat(it.message).isEqualTo("You have no access to this data")
            }

            verify(exactly = 0) { friendService.addFriend(any(), any()) }
        }
    }

    @Nested
    inner class HasAttribute {
        @Test
        fun `correctly checking that friend has an attribute`() {
            val userId = 1L
            val friendId = 2L
            val attributeName = "Age"
            val accessToken = "valid-access-token"

            every { authService.getAuthorizedUserId() } returns userId
            every { authService.authorizeUser(accessToken) } returns userId
            every { friendService.hasAttribute(userId, friendId, attributeName) } returns true

            mockMvc.get("/friends/${friendId}/has_attribute/${attributeName}") {
                header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            }.andExpect {
                status { isOk() }
                content {
                    json(
                        """
                {
                    "message": "Attribute exists check",
                    "data": "true"
                }
                """
                    )
                }
            }
        }

        @Test
        fun `correctly checking that friend does not have an attribute`() {
            val userId = 1L
            val friendId = 2L
            val attributeName = "Age"
            val accessToken = "valid-access-token"

            every { authService.getAuthorizedUserId() } returns userId
            every { authService.authorizeUser(accessToken) } returns userId

            every { friendService.hasAttribute(userId, friendId, attributeName) } returns false

            mockMvc.get("/friends/${friendId}/has_attribute/${attributeName}") {
                header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            }.andExpect {
                status { isOk() }
                content {
                    json(
                        """
                {
                    "message": "Attribute exists check",
                    "data": "false"
                }
                """
                    )
                }
            }
        }

        @Test
        fun `trying to check if friend has attribute by unauthorized user`() {
            val friendId = 1L
            val attributeName = "Age"

            mockMvc.get("/friends/${friendId}/has_attribute/${attributeName}") {
                contentType = MediaType.APPLICATION_JSON
                accept = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isForbidden() }
            }
        }

        @Test
        fun `trying to check if friend which does not belong to authorized user has attribute `() {
            val friendId = 1L
            val attributeName = "Age"
            val accessToken = "invalid-access-token"

            every { authService.authorizeUser(accessToken) } throws AccessDeniedException()

            assertThrows<AccessDeniedException> {
                mockMvc.get("/friends/${friendId}/has_attribute/${attributeName}") {
                    contentType = MediaType.APPLICATION_JSON
                    accept = MediaType.APPLICATION_JSON
                    header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
                }
            }.also {
                assertThat(it.message).isEqualTo("You have no access to this data")
            }

            verify(exactly = 0) { friendService.addFriend(any(), any()) }
        }
    }

    @Nested
    inner class GetAttributes {
        @Test
        fun `correctly getting attributes`() {
            val userId = 1L
            val friendId = 2L
            val accessToken = "valid-access-token"
            val attributeDTO = AttributeDTO("Age", "25")
            val friendDTO = FriendDTO(friendId, "John", userId, mutableMapOf(attributeDTO.name to attributeDTO.value))

            every { authService.getAuthorizedUserId() } returns userId
            every { authService.authorizeUser(accessToken) } returns userId
            every { friendService.getAttributes(userId, friendId) } returns listOf(attributeDTO)

            mockMvc.get("/friends/${friendDTO.id}/attributes") {
                contentType = MediaType.APPLICATION_JSON
                header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            }.andExpect {
                status { isOk() }
                content {
                    json(
                        """
                {
                    "message": "Attributes retrieved",
                    "data": [
                        {
                            "name": "${attributeDTO.name}",
                            "value": "${attributeDTO.value}"
                        }
                    ]
                }
                """
                    )
                }
            }
        }

        @Test
        fun `trying to get attributes of a friend that does not exist`() {
            val userId = 1L
            val friendId = 2L
            val accessToken = "valid-access-token"

            every { authService.getAuthorizedUserId() } returns userId
            every { authService.authorizeUser(accessToken) } returns userId

            every { friendService.getAttributes(userId, friendId) } throws FriendDoesNotExistException()

            mockMvc.get("/friends/${friendId}/attributes") {
                contentType = MediaType.APPLICATION_JSON
                header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            }.andExpect {
                status { isNotFound() }
                content {
                    json("""{"message": "Friend does not exist"}""")
                }
            }
        }

        @Test
        fun `trying to get attributes by unauthorized user`() {
            val friendId = 1L

            mockMvc.get("/friends/${friendId}/attributes") {
                contentType = MediaType.APPLICATION_JSON
                accept = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isForbidden() }
            }
        }

        @Test
        fun `trying to get attributes of a friend that does not belong to authorized user`() {
            val friendId = 1L
            val accessToken = "invalid-access-token"

            every { authService.authorizeUser(accessToken) } throws AccessDeniedException()

            assertThrows<AccessDeniedException> {
                mockMvc.get("/friends/${friendId}/attributes") {
                    contentType = MediaType.APPLICATION_JSON
                    accept = MediaType.APPLICATION_JSON
                    header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
                }
            }.also {
                assertThat(it.message).isEqualTo("You have no access to this data")
            }

            verify(exactly = 0) { friendService.addFriend(any(), any()) }
        }
    }

    @Nested
    inner class GetAttributeNames {
        @Test
        fun `correctly getting attribute names`() {
            val userId = 1L
            val friendId = 2L
            val accessToken = "valid-access-token"
            val attributeNames = listOf("Age", "Favorite artist")

            every { authService.getAuthorizedUserId() } returns userId
            every { authService.authorizeUser(accessToken) } returns userId
            every { friendService.getAttributeNames(userId, friendId) } returns attributeNames

            mockMvc.get("/friends/$friendId/attribute_names") {
                header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            }.andExpect {
                status { isOk() }
                content {
                    json(
                        """
                    {
                        "message": "Attribute names retrieved",
                        "data": ${asJsonString(attributeNames)}
                    }
                    """
                    )
                }
            }
        }

        @Test
        fun `trying to get attribute names of a friend which does not exist`() {
            val userId = 1L
            val friendId = 2L
            val accessToken = "valid-access-token"

            every { authService.getAuthorizedUserId() } returns userId
            every { authService.authorizeUser(accessToken) } returns userId
            every { friendService.getAttributeNames(userId, friendId) } throws FriendDoesNotExistException()

            mockMvc.get("/friends/$friendId/attribute_names") {
                header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            }.andExpect {
                status { isNotFound() }
                content {
                    json("""{"message": "Friend does not exist"}""")
                }
            }
        }

        @Test
        fun `trying to get attribute names of a friend by unauthorized user`() {
            val friendId = 1L

            mockMvc.get("/friends/$friendId/attribute_names") {
                contentType = MediaType.APPLICATION_JSON
                accept = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isForbidden() }
            }
        }

        @Test
        fun `trying to get attribute names of a friend which does not belong to authorized user`() {
            val friendId = 1L
            val accessToken = "invalid-access-token"

            every { authService.authorizeUser(accessToken) } throws AccessDeniedException()

            assertThrows<AccessDeniedException> {
                mockMvc.get("/friends/$friendId/attribute_names") {
                    contentType = MediaType.APPLICATION_JSON
                    accept = MediaType.APPLICATION_JSON
                    header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
                }
            }.also {
                assertThat(it.message).isEqualTo("You have no access to this data")
            }

            verify(exactly = 0) { friendService.addFriend(any(), any()) }
        }
    }

    @Nested
    inner class UpdateAttribute {
        @Test
        fun `correctly updating attribute`() {
            val userId = 1L
            val friendId = 2L
            val accessToken = "valid-access-token"
            val friendDTO = FriendDTO(friendId, "John", userId, mutableMapOf("Age" to "25"))
            val attribute = AttributeDTO("Age", "30")
            val jsonRequest = asJsonString(attribute)

            every { authService.getAuthorizedUserId() } returns userId
            every { authService.authorizeUser(accessToken) } returns userId
            every {
                friendService.updateAttribute(
                    userId,
                    friendId,
                    attribute
                )
            } returns friendDTO.copy(attributes = mutableMapOf(attribute.name to attribute.value))

            mockMvc.put("/friends/${friendDTO.id}/update_attribute") {
                contentType = MediaType.APPLICATION_JSON
                content = jsonRequest
                header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            }.andExpect {
                status { isOk() }
                content {
                    json(
                        """
                {
                    "message": "Attribute updated",
                    "data": {
                        "id": 2,
                        "name": "John",
                        "ownerId": 1,
                        "attributes": {"${attribute.name}": "${attribute.value}"}
                    }
                }
                """
                    )
                }
            }
        }

        @Test
        fun `trying to update an attribute which does not exist`() {
            val userId = 1L
            val friendId = 2L
            val accessToken = "valid-access-token"
            val attribute = AttributeDTO("Age", "25")
            val jsonRequest = asJsonString(attribute)

            every { authService.getAuthorizedUserId() } returns userId
            every { authService.authorizeUser(accessToken) } returns userId
            every { friendService.updateAttribute(userId, friendId, attribute) } throws AttributeDoesNotExistException()

            mockMvc.put("/friends/${friendId}/update_attribute") {
                contentType = MediaType.APPLICATION_JSON
                content = jsonRequest
                header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            }.andExpect {
                status { isNotFound() }
                content {
                    json("""{"message": "Attribute does not exist"}""")
                }
            }
        }

        @Test
        fun `trying to update an attribute of a friend that does not exist`() {
            val userId = 1L
            val friendId = 2L
            val accessToken = "valid-access-token"
            val attribute = AttributeDTO("Age", "25")
            val jsonRequest = asJsonString(attribute)

            every { authService.getAuthorizedUserId() } returns userId
            every { authService.authorizeUser(accessToken) } returns userId
            every { friendService.updateAttribute(userId, friendId, attribute) } throws FriendDoesNotExistException()

            mockMvc.put("/friends/${friendId}/update_attribute") {
                contentType = MediaType.APPLICATION_JSON
                content = jsonRequest
                header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            }.andExpect {
                status { isNotFound() }
                content {
                    json("""{"message": "Friend does not exist"}""")
                }
            }
        }

        @Test
        fun `trying to update friend's attribute by unauthorized user`() {
            val friendId = 1L
            val attribute = AttributeDTO("Age", "30")
            val jsonRequest = asJsonString(attribute)

            mockMvc.put("/friends/${friendId}/update_attribute") {
                contentType = MediaType.APPLICATION_JSON
                accept = MediaType.APPLICATION_JSON
                content = jsonRequest
            }.andExpect {
                status { isForbidden() }
            }
        }

        @Test
        fun `trying to update friend's attribute of a friend that does not belong to authorized user`() {
            val friendId = 1L
            val accessToken = "invalid-access-token"
            val attribute = AttributeDTO("Age", "30")
            val jsonRequest = asJsonString(attribute)

            every { authService.authorizeUser(accessToken) } throws AccessDeniedException()

            assertThrows<AccessDeniedException> {
                mockMvc.put("/friends/${friendId}/update_attribute") {
                    contentType = MediaType.APPLICATION_JSON
                    accept = MediaType.APPLICATION_JSON
                    header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
                    content = jsonRequest
                }
            }.also {
                assertThat(it.message).isEqualTo("You have no access to this data")
            }

            verify(exactly = 0) { friendService.addFriend(any(), any()) }
        }
    }

    @Nested
    inner class RemoveAttribute {
        @Test
        fun `correctly removing friend's attribute`() {
            val userId = 1L
            val friendId = 2L
            val accessToken = "valid-access-token"
            val friendDTO = FriendDTO(friendId, "John", userId, mutableMapOf("Age" to "25"))
            val attributeToDelete = "Age"

            every { authService.getAuthorizedUserId() } returns userId
            every { authService.authorizeUser(accessToken) } returns userId
            every {
                friendService.removeAttribute(
                    userId,
                    friendId,
                    attributeToDelete
                )
            } returns friendDTO.copy(attributes = mutableMapOf())

            mockMvc.delete("/friends/${friendDTO.id}/delete_attribute") {
                contentType = MediaType.APPLICATION_JSON
                content = attributeToDelete
                header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            }.andExpect {
                status { isOk() }
                content {
                    json(
                        """
                {
                    "message": "Attribute deleted",
                    "data": {
                        "id": 2,
                        "name": "John",
                        "ownerId": 1,
                        "attributes": {}
                    }
                }
                """
                    )
                }
            }
        }

        @Test
        fun `trying to remove an attribute that does not exist`() {
            val userId = 1L
            val friendId = 2L
            val accessToken = "valid-access-token"
            val attributeName = "Age"

            every { authService.getAuthorizedUserId() } returns userId
            every { authService.authorizeUser(accessToken) } returns userId
            every {
                friendService.removeAttribute(
                    userId,
                    friendId,
                    attributeName
                )
            } throws AttributeDoesNotExistException()


            mockMvc.delete("/friends/$friendId/delete_attribute") {
                contentType = MediaType.APPLICATION_JSON
                content = attributeName
                header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            }.andExpect {
                status { isNotFound() }
                content {
                    json("""{"message": "Attribute does not exist"}""")
                }
            }
        }

        @Test
        fun `trying to remove an attribute of a friend that does not exist`() {
            val userId = 1L
            val friendId = 2L
            val accessToken = "valid-access-token"
            val attributeName = "Age"

            every { authService.getAuthorizedUserId() } returns userId
            every { authService.authorizeUser(accessToken) } returns userId
            every {
                friendService.removeAttribute(
                    userId,
                    friendId,
                    attributeName
                )
            } throws FriendDoesNotExistException()

            mockMvc.delete("/friends/$friendId/delete_attribute") {
                contentType = MediaType.APPLICATION_JSON
                content = attributeName
                header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            }.andExpect {
                status { isNotFound() }
                content { json("""{"message": "Friend does not exist"}""") }
            }
        }

        @Test
        fun `trying to remove friend's attribute by unauthorized user`() {
            val friendId = 1L

            mockMvc.delete("/friends/$friendId/delete_attribute") {
                contentType = MediaType.APPLICATION_JSON
                accept = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isForbidden() }
            }
        }

        @Test
        fun `trying to remove friend's attribute of a friend that does not belong to authorized user`() {
            val friendId = 1L
            val accessToken = "invalid-access-token"

            every { authService.authorizeUser(accessToken) } throws AccessDeniedException()

            assertThrows<AccessDeniedException> {
                mockMvc.delete("/friends/$friendId/delete_attribute") {
                    contentType = MediaType.APPLICATION_JSON
                    accept = MediaType.APPLICATION_JSON
                    header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
                }
            }.also {
                assertThat(it.message).isEqualTo("You have no access to this data")
            }

            verify(exactly = 0) { friendService.addFriend(any(), any()) }
        }
    }

    fun asJsonString(obj: Any?): String {
        return try {
            val mapper = ObjectMapper()
            mapper.writeValueAsString(obj)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}