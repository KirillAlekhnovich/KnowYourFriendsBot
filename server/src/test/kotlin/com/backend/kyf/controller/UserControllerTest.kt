package com.backend.kyf.controller

import com.backend.kyf.dto.UserDTO
import com.backend.kyf.entity.Friend
import com.backend.kyf.entity.User
import com.backend.kyf.exception.AttributeAlreadyExistsException
import com.backend.kyf.exception.AttributeDoesNotExistException
import com.backend.kyf.exception.InvalidAttributeNameException
import com.backend.kyf.exception.UserAlreadyExistsException
import com.backend.kyf.service.UserService
import com.backend.kyf.utils.RedisParams
import com.backend.kyf.utils.auth.AuthService
import com.backend.kyf.utils.auth.Jedis
import com.ninjasquad.springmockk.MockkBean
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.*
import org.springframework.test.web.servlet.request.RequestPostProcessor


@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest(@Autowired val mockMvc: MockMvc) {

    @MockkBean
    private lateinit var userService: UserService

    @MockkBean
    private lateinit var authService: AuthService

    @BeforeEach
    fun setup() {
        clearAllMocks()
    }

    @Nested
    inner class CreateUser {

        @Test
        fun `correctly creating a user`() {
            val userId = 1L
            val accessToken = "valid-access-token"
            val validIp = System.getenv("AllowedIPs").split(",").first()

            every { userService.registerUser(userId) } returns UserDTO(userId, mutableSetOf(), mutableSetOf())
            every { Jedis.getValue(userId, RedisParams.ACCESS_TOKEN.name) } returns accessToken

            mockMvc.post("/users/$userId") {
                with(setRemoteAddr(validIp))
            }.andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content {
                    json(
                        """
                    {
                        "message": "User created",
                        "data": "$accessToken"
                    }
                    """.trimIndent()
                    )
                }
            }

            verify { userService.registerUser(userId) }
        }

        @Test
        fun `trying to add a user from invalid client IP address`() {
            val userId = 1L
            val invalidIp = "123.456.789.0"

            mockMvc.post("/users/$userId") {
                with(setRemoteAddr(invalidIp))
            }.andExpect {
                status { isBadRequest() }
            }

            verify(exactly = 0) { userService.registerUser(any()) }
        }

        @Test
        fun `trying to create user with received invalid access token`() {
            val userId = 1L
            val validIp = System.getenv("AllowedIPs").split(",").first()

            every { userService.registerUser(userId) } returns UserDTO(userId, mutableSetOf(), mutableSetOf())
            every { Jedis.getValue(userId, RedisParams.ACCESS_TOKEN.name) } returns null

            mockMvc.post("/users/$userId") {
                with(setRemoteAddr(validIp))
            }.andExpect {
                status { isBadRequest() }
            }

            verify { userService.registerUser(userId) }
        }

        @Test
        fun `trying to create a user that already exists`() {
            val userId = 1L
            val validIp = System.getenv("AllowedIPs").split(",").first()

            every { userService.registerUser(userId) } throws UserAlreadyExistsException()

            mockMvc.post("/users/$userId") {
                with(setRemoteAddr(validIp))
            }.andExpect {
                status { isConflict() }
                content {
                    json(
                        """
                    {
                        "message": "User already exists"
                    }
                    """.trimIndent()
                    )
                }
            }

            verify { userService.registerUser(userId) }
        }
    }

    @Nested
    inner class GetUser {
        @Test
        fun `correctly getting user`() {
            val userId = 1L
            val accessToken = "valid-access-token"
            val user = UserDTO(userId, mutableSetOf(), mutableSetOf())

            every { userService.getUserDTOById(userId) } returns user
            every { authService.authorizeUser(any()) } returns userId

            mockMvc.get("/users/$userId") {
                header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            }.andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content {
                    json(
                        """
                {
                    "message": "User retrieved",
                    "data": {
                        "id": $userId,
                        "friends": [],
                        "generalAttributes": []
                    }
                }
                """.trimIndent()
                    )
                }
            }

            verify { userService.getUserDTOById(userId) }
        }

        @Test
        fun `trying to create a user with invalid access token`() {
            val userId = 1L
            val accessToken = "invalid-access-token"

            every { authService.authorizeUser(accessToken) } returns 2L

            mockMvc.get("/users/$userId") {
                header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            }.andExpect {
                status { isForbidden() }
            }

            verify(exactly = 0) { userService.getUserDTOById(userId) }
        }
    }

    @Nested
    inner class UserExists {
        @Test
        fun `correctly checking that user exists`() {
            val userId = 1L
            val validIp = System.getenv("AllowedIPs").split(",").first()

            every { userService.exists(userId) } returns true

            mockMvc.get("/users/exists/$userId") {
                with(setRemoteAddr(validIp))
            }.andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content {
                    json(
                        """
                {
                    "message": "User exists check",
                    "data": "true"
                }
                """.trimIndent()
                    )
                }
            }

            verify { userService.exists(userId) }
        }

        @Test
        fun `correctly checking that user does not exist`() {
            val userId = 1L
            val validIp = System.getenv("AllowedIPs").split(",").first()

            every { userService.exists(userId) } returns false

            mockMvc.get("/users/exists/$userId") {
                with(setRemoteAddr(validIp))
            }.andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content {
                    json(
                        """
                {
                    "message": "User exists check",
                    "data": "false"
                }
                """.trimIndent()
                    )
                }
            }

            verify { userService.exists(userId) }
        }

        @Test
        fun `trying to check if user exists from invalid client IP address`() {
            val userId = 1L
            val invalidIp = "123.456.789.0"

            mockMvc.get("/users/exists/$userId") {
                with(setRemoteAddr(invalidIp))
            }.andExpect {
                status { isBadRequest() }
            }

            verify(exactly = 0) { userService.exists(userId) }
        }
    }

    @Nested
    inner class ResetUser {
        @Test
        fun `correctly resetting user`() {
            val userId = 1L
            val accessToken = "valid-access-token"
            val user = UserDTO(userId, mutableSetOf(), mutableSetOf())

            every { authService.authorizeUser(any()) } returns userId
            every { userService.getUserDTOById(userId) } returns user
            every { userService.reset(userId) } just runs

            mockMvc.put("/users/$userId/reset") {
                header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            }.andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content {
                    json(
                        """
                {
                    "message": "Your profile has been reset",
                    "data": {
                        "id": $userId,
                        "friends": [],
                        "generalAttributes": []
                    }
                }
                """.trimIndent()
                    )
                }
            }

            verify { authService.authorizeUser(accessToken) }
            verify { userService.reset(userId) }
        }

        @Test
        fun `trying to reset a user with invalid access token`() {
            val userId = 1L
            val accessToken = "invalid-access-token"

            every { authService.authorizeUser(accessToken) } returns 2L

            mockMvc.put("/users/$userId/reset") {
                header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            }.andExpect {
                status { isForbidden() }
            }

            verify(exactly = 0) { userService.getUserDTOById(userId) }
        }
    }

    @Nested
    inner class AddGeneralAttribute {
        @Test
        fun `correctly adding general attribute`() {
            val userId = 1L
            val accessToken = "valid-access-token"
            val attributeName = "Age"
            val user = User(userId, mutableSetOf(), mutableSetOf())
            val friend1 = Friend(2L, "Johny", userId, mutableMapOf())
            val friend2 = Friend(3L, "Billy", userId, mutableMapOf())
            user.friends.addAll(listOf(friend1, friend2))

            every { userService.getUserById(userId) } returns user
            every { authService.authorizeUser(any()) } returns userId
            every { userService.addGeneralAttribute(userId, attributeName) } just runs

            mockMvc.put("/users/$userId/add_general_attribute") {
                header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
                contentType = MediaType.APPLICATION_JSON
                content = attributeName
            }.andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content {
                    json(
                        """
            {
                "message": "Attribute $attributeName has been added to all your friends",
                "data": {}
            }
            """.trimIndent()
                    )
                }
            }

            verify { userService.addGeneralAttribute(userId, attributeName) }
        }

        @Test
        fun `trying to create general attribute with invalid attribute name`() {
            val userId = 1L
            val accessToken = "valid-access-token"
            val attributeName = "Ag#"
            val user = User(userId, mutableSetOf(), mutableSetOf())

            every { userService.getUserById(userId) } returns user
            every { authService.authorizeUser(any()) } returns userId
            every { userService.addGeneralAttribute(userId, attributeName) } throws InvalidAttributeNameException()

            mockMvc.put("/users/$userId/add_general_attribute") {
                header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
                contentType = MediaType.APPLICATION_JSON
                content = attributeName
            }.andExpect {
                status { isNotAcceptable() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content {
                    json(
                        """
        {
            "message": "Attribute name is invalid"
        }
        """.trimIndent()
                    )
                }
            }

            verify { authService.authorizeUser(any()) }
            verify { userService.addGeneralAttribute(userId, attributeName) }
        }

        @Test
        fun `trying to create general attribute which already exists`() {
            val userId = 1L
            val accessToken = "valid-access-token"
            val attributeName = "Age"
            val user = User(userId, mutableSetOf(), mutableSetOf(attributeName))
            val friend1 = Friend(2L, "Johny", userId, mutableMapOf(attributeName to "value"))
            val friend2 = Friend(3L, "Billy", userId, mutableMapOf(attributeName to "value"))
            user.friends.addAll(setOf(friend1, friend2))

            every { userService.getUserById(userId) } returns user
            every { authService.authorizeUser(any()) } returns userId
            every { userService.addGeneralAttribute(userId, attributeName) } throws AttributeAlreadyExistsException()

            mockMvc.put("/users/$userId/add_general_attribute") {
                header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
                contentType = MediaType.APPLICATION_JSON
                content = attributeName
            }.andExpect {
                status { isConflict() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content {
                    json(
                        """
                {
                    "message": "Attribute already exists"
                }
                """.trimIndent()
                    )
                }
            }

            verify { authService.authorizeUser(any()) }
            verify { userService.addGeneralAttribute(userId, attributeName) }
        }

        @Test
        fun `trying to add general attribute to a user with invalid access token`() {
            val userId = 1L
            val accessToken = "invalid-access-token"
            val attributeName = "Age"

            every { authService.authorizeUser(accessToken) } returns 2L

            mockMvc.put("/users/$userId/add_general_attribute") {
                header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
                content = attributeName
            }.andExpect {
                status { isForbidden() }
            }

            verify(exactly = 0) { userService.getUserDTOById(userId) }
        }
    }

    @Nested
    inner class HasGeneralAttribute {
        @Test
        fun `correctly checking that attribute exists`() {
            val userId = 1L
            val accessToken = "valid-access-token"
            val attributeName = "Age"
            val user = User(userId, mutableSetOf(), mutableSetOf(attributeName))

            every { userService.getUserById(userId) } returns user
            every { authService.authorizeUser(any()) } returns userId
            every { userService.hasGeneralAttribute(userId, attributeName) } returns true

            mockMvc.get("/users/$userId/has_general_attribute/$attributeName") {
                header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            }.andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content {
                    json(
                        """
                {
                    "message": "General attribute check",
                    "data": "true"
                }
                """.trimIndent()
                    )
                }
            }

            verify { userService.hasGeneralAttribute(userId, attributeName) }
        }

        @Test
        fun `correctly checking that attribute does not exist`() {
            val userId = 1L
            val accessToken = "valid-access-token"
            val attributeName = "Eye color"
            val user = User(userId, mutableSetOf(), mutableSetOf("Age"))

            every { userService.getUserById(userId) } returns user
            every { authService.authorizeUser(any()) } returns userId
            every { userService.hasGeneralAttribute(userId, attributeName) } returns false

            mockMvc.get("/users/$userId/has_general_attribute/$attributeName") {
                header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            }.andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content {
                    json(
                        """
                {
                    "message": "General attribute check",
                    "data": "false"
                }
                """.trimIndent()
                    )
                }
            }

            verify { authService.authorizeUser(any()) }
            verify { userService.hasGeneralAttribute(userId, attributeName) }
        }

        @Test
        fun `trying to check that user has a general attribute with invalid access token`() {
            val userId = 1L
            val accessToken = "invalid-access-token"
            val attributeName = "Age"

            every { authService.authorizeUser(accessToken) } returns 2L

            mockMvc.get("/users/$userId/has_general_attribute/$attributeName") {
                header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            }.andExpect {
                status { isForbidden() }
            }

            verify(exactly = 0) { userService.getUserDTOById(userId) }
        }
    }

    @Nested
    inner class RemoveGeneralAttribute {
        @Test
        fun `correctly removing general attribute from all friends`() {
            val userId = 1L
            val accessToken = "valid-access-token"
            val attributeName = "Age"
            val friend1 = Friend(2L, "Johny", userId, mutableMapOf(attributeName to "21"))
            val friend2 = Friend(3L, "Billy", userId, mutableMapOf(attributeName to "22"))
            val user = User(
                userId,
                mutableSetOf(friend1, friend2),
                mutableSetOf(attributeName)
            )

            every { userService.getUserById(userId) } returns user
            every { authService.authorizeUser(any()) } returns userId
            every { userService.removeGeneralAttribute(userId, attributeName) } just runs

            mockMvc.delete("/users/$userId/remove_general_attribute") {
                header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
                contentType = MediaType.APPLICATION_JSON
                content = attributeName
            }.andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content {
                    json(
                        """
            {
                "message": "Attribute $attributeName has been removed from all your friends",
                "data": {}
            }
            """.trimIndent()
                    )
                }
            }

            verify { authService.authorizeUser(any()) }
            verify { userService.removeGeneralAttribute(userId, attributeName) }
        }

        @Test
        fun `trying to remove an attribute which does not exist`() {
            val userId = 1L
            val accessToken = "valid-access-token"
            val attributeName = "Eye color"
            val user = User(userId, mutableSetOf(), mutableSetOf())

            every { userService.getUserById(userId) } returns user
            every { authService.authorizeUser(any()) } returns userId
            every { userService.removeGeneralAttribute(userId, attributeName) } throws AttributeDoesNotExistException()

            mockMvc.delete("/users/$userId/remove_general_attribute") {
                header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
                contentType = MediaType.APPLICATION_JSON
                content = attributeName
            }.andExpect {
                status { isNotFound() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content {
                    json(
                        """
        {
            "message": "Attribute does not exist"
        }
        """.trimIndent()
                    )
                }
            }

            verify { authService.authorizeUser(any()) }
            verify { userService.removeGeneralAttribute(userId, attributeName) }
        }

        @Test
        fun `trying to remove a general attribute from a user with invalid access token`() {
            val userId = 1L
            val accessToken = "invalid-access-token"
            val attributeName = "Age"

            every { authService.authorizeUser(accessToken) } returns 2L

            mockMvc.delete("/users/$userId/remove_general_attribute") {
                header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
                content = attributeName
            }.andExpect {
                status { isForbidden() }
            }

            verify(exactly = 0) { userService.getUserDTOById(userId) }
        }
    }

    private fun setRemoteAddr(remoteAddr: String): RequestPostProcessor {
        return RequestPostProcessor { request ->
            request.remoteAddr = remoteAddr
            request
        }
    }
}
