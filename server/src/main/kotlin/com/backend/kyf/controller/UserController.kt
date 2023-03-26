package com.backend.kyf.controller

import com.backend.kyf.service.UserService
import com.backend.kyf.utils.RedisParams
import com.backend.kyf.utils.ResponseBuilder.buildResponse
import com.backend.kyf.utils.auth.ClientIPs
import com.backend.kyf.utils.auth.Jedis.getValue
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

/**
 * Controller for handling user-related requests.
 */
@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService
) {

    /**
     * Creates a new user.
     */
    @PostMapping("/{userId}")
    fun createUser(@PathVariable userId: Long, request: HttpServletRequest): ResponseEntity<Any> {
        if (request.remoteAddr !in ClientIPs.get()) {
            return ResponseEntity.badRequest().body("IP address is not allowed to connect to this server")
        }
        userService.registerUser(userId)
        val accessToken = getValue(userId, RedisParams.ACCESS_TOKEN.name)
        return if (accessToken == null) ResponseEntity.badRequest().body("Failed to create user")
        else ResponseEntity.ok(buildResponse("User created", accessToken))
    }

    /**
     * Gets user info by id.
     */
    @GetMapping("/{userId}")
    @PreAuthorize("#userId == authentication.principal")
    fun getUser(@PathVariable("userId") userId: Long): ResponseEntity<Any> {
        return ResponseEntity.ok(buildResponse("User retrieved", userService.getUserDTOById(userId)))
    }

    /**
     * Checks whether user exists or not.
     */
    @GetMapping("/exists/{userId}")
    fun userExists(@PathVariable userId: Long, request: HttpServletRequest): ResponseEntity<Any> {
        if (request.remoteAddr !in ClientIPs.get()) {
            return ResponseEntity.badRequest().body("IP address is not allowed to connect to this server")
        }
        return ResponseEntity.ok(buildResponse("User exists check", userService.exists(userId).toString()))
    }

    /**
     * Resets user's profile.
     */
    @PutMapping("/{userId}/reset")
    @PreAuthorize("#userId == authentication.principal")
    fun resetUser(@PathVariable userId: Long): ResponseEntity<Any> {
        userService.reset(userId)
        return ResponseEntity.ok(
            buildResponse("Your profile has been reset", userService.getUserDTOById(userId))
        )
    }

    /**
     * Adds a general attribute to all friends.
     */
    @PutMapping("/{userId}/add_general_attribute")
    @PreAuthorize("#userId == authentication.principal")
    fun addGeneralAttribute(@PathVariable userId: Long, @RequestBody attributeName: String): ResponseEntity<Any> {
        return ResponseEntity.ok(
            buildResponse(
                "Attribute $attributeName has been added to all your friends",
                userService.addGeneralAttribute(userId, attributeName)
            )
        )
    }

    /**
     * Checks whether a general attribute exists or not.
     */
    @GetMapping("/{userId}/has_general_attribute/{attributeName}")
    @PreAuthorize("#userId == authentication.principal")
    fun hasGeneralAttribute(@PathVariable userId: Long, @PathVariable attributeName: String): ResponseEntity<Any> {
        return ResponseEntity.ok(
            buildResponse(
                "General attribute check",
                userService.hasGeneralAttribute(userId, attributeName).toString()
            )
        )
    }

    /**
     * Removes a general attribute from all friends.
     */
    @DeleteMapping("/{userId}/remove_general_attribute")
    @PreAuthorize("#userId == authentication.principal")
    fun removeGeneralAttribute(@PathVariable userId: Long, @RequestBody attributeName: String): ResponseEntity<Any> {
        return ResponseEntity.ok(
            buildResponse(
                "Attribute $attributeName has been removed from all your friends",
                userService.removeGeneralAttribute(userId, attributeName)
            )
        )
    }
}