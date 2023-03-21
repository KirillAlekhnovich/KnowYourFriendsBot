package com.backend.kyf.controller

import com.backend.kyf.dto.FriendDTO
import com.backend.kyf.service.UserService
import com.backend.kyf.utils.auth.ClientIPs
import com.backend.kyf.utils.auth.Jedis
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService
) {

    fun generateResponseJson(message: String, dto: Any): MutableMap<String, Any?> {
        val body: MutableMap<String, Any?> = LinkedHashMap()
        body["timestamp"] = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))
        body["message"] = message
        body["data"] = dto
        return body
    }

    @PostMapping("/{userId}")
    fun createUser(@PathVariable userId: Long, request: HttpServletRequest): ResponseEntity<Any> {
        if (request.remoteAddr !in ClientIPs.get()) {
            return ResponseEntity.badRequest().body("IP address is not allowed to connect to this server")
        }
        userService.registerUser(userId)
        return ResponseEntity.ok(generateResponseJson("User created", Jedis.get().hget(userId.toString(), "accessToken")))
    }

    @GetMapping("/exists/{userId}")
    fun userExists(@PathVariable userId: Long, request: HttpServletRequest): ResponseEntity<Any> {
        if (request.remoteAddr !in ClientIPs.get()) {
            return ResponseEntity.badRequest().body("IP address is not allowed to connect to this server")
        }
        return ResponseEntity.ok(generateResponseJson("User exists check", userService.exists(userId).toString()))
    }

    @GetMapping("/{userId}")
    @PreAuthorize("#userId == authentication.principal")
    fun getUser(@PathVariable("userId") userId: Long): ResponseEntity<Any> {
        return ResponseEntity.ok(generateResponseJson("User retrieved", userService.getUserDTOById(userId)))
    }

    @PutMapping("/{userId}/reset")
    @PreAuthorize("#userId == authentication.principal")
    fun resetUser(@PathVariable userId: Long): ResponseEntity<Any> {
        userService.reset(userId)
        return ResponseEntity.ok(
            generateResponseJson(
                "Your profile has been reset",
                userService.getUserDTOById(userId)
            )
        )
    }

    @PutMapping("/{userId}/add_friend")
    @PreAuthorize("#userId == authentication.principal")
    fun addFriend(@PathVariable userId: Long, @RequestBody friendDTO: FriendDTO): ResponseEntity<Any> {
        return ResponseEntity.ok(generateResponseJson("Friend added", userService.addFriend(userId, friendDTO)))
    }

    @GetMapping("/{userId}/friends/{friendName}")
    @PreAuthorize("#userId == authentication.principal")
    fun getFriendByName(@PathVariable userId: Long, @PathVariable friendName: String): ResponseEntity<Any> {
        return ResponseEntity.ok(
            generateResponseJson(
                "Friend retrieved",
                userService.getFriendByName(userId, friendName)
            )
        )
    }

    @GetMapping("/{userId}/friends/names")
    @PreAuthorize("#userId == authentication.principal")
    fun getAllFriendNames(@PathVariable userId: Long): ResponseEntity<Any> {
        return ResponseEntity.ok(generateResponseJson("List of friend names retrieved", userService.getFriendNames(userId)))
    }

    @GetMapping("/{userId}/friends")
    @PreAuthorize("#userId == authentication.principal")
    fun getAllFriends(@PathVariable userId: Long): ResponseEntity<Any> {
        return ResponseEntity.ok(generateResponseJson("List of friends retrieved", userService.getAllFriends(userId)))
    }

    @PutMapping("/{userId}/remove_friend/{friendId}")
    @PreAuthorize("#userId == authentication.principal")
    fun removeFriend(@PathVariable userId: Long, @PathVariable friendId: Long): ResponseEntity<Any> {
        return ResponseEntity.ok(generateResponseJson("Friend removed", userService.removeFriend(userId, friendId)))
    }

    @PutMapping("/{userId}/add_general_attribute")
    @PreAuthorize("#userId == authentication.principal")
    fun addGeneralAttribute(@PathVariable userId: Long, @RequestBody attributeName: String): ResponseEntity<Any> {
        return ResponseEntity.ok(
            generateResponseJson(
                "Attribute $attributeName has been added to all your friends",
                userService.addGeneralAttribute(userId, attributeName)
            )
        )
    }

    @GetMapping("/{userId}/has_general_attribute/{attributeName}")
    @PreAuthorize("#userId == authentication.principal")
    fun hasGeneralAttribute(@PathVariable userId: Long, @PathVariable attributeName: String): ResponseEntity<Any> {
        return ResponseEntity.ok(
            generateResponseJson(
                "General attribute check",
                userService.hasGeneralAttribute(userId, attributeName).toString()
            )
        )
    }

    @DeleteMapping("/{userId}/remove_general_attribute")
    @PreAuthorize("#userId == authentication.principal")
    fun removeGeneralAttribute(@PathVariable userId: Long, @RequestBody attributeName: String): ResponseEntity<Any> {
        return ResponseEntity.ok(
            generateResponseJson(
                "Attribute $attributeName has been removed from all your friends",
                userService.removeGeneralAttribute(userId, attributeName)
            )
        )
    }
}