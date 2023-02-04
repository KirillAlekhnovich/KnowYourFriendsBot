package com.backend.kyf.controller

import com.backend.kyf.dto.FriendDTO
import com.backend.kyf.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
    fun createUser(@PathVariable userId: Long): ResponseEntity<Any> {
        return ResponseEntity.ok(generateResponseJson("User created", userService.registerUser(userId)))
    }

    @GetMapping("/{userId}")
    fun getUser(@PathVariable userId: Long): ResponseEntity<Any> {
        return ResponseEntity.ok(generateResponseJson("User retrieved", userService.getUserDTOById(userId)))
    }

    @GetMapping("/exists/{userId}")
    fun userExists(@PathVariable userId: Long): ResponseEntity<Any> {
        return ResponseEntity.ok(generateResponseJson("User exists check", userService.exists(userId).toString()))
    }

    @PutMapping("/{userId}/reset")
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
    fun addFriend(@PathVariable userId: Long, @RequestBody friendDTO: FriendDTO): ResponseEntity<Any> {
        return ResponseEntity.ok(generateResponseJson("Friend added", userService.addFriend(userId, friendDTO)))
    }

    @GetMapping("/{userId}/friends/{friendName}")
    fun getFriendByName(@PathVariable userId: Long, @PathVariable friendName: String): ResponseEntity<Any> {
        return ResponseEntity.ok(
            generateResponseJson(
                "Friend retrieved",
                userService.getFriendByName(userId, friendName)
            )
        )
    }

    @GetMapping("/{userId}/friends")
    fun getAllFriends(@PathVariable userId: Long): ResponseEntity<Any> {
        return ResponseEntity.ok(generateResponseJson("List of friends retrieved", userService.getAllFriends(userId)))
    }

    @PutMapping("/{userId}/remove_friend/{friendId}")
    fun removeFriend(@PathVariable userId: Long, @PathVariable friendId: Long): ResponseEntity<Any> {
        return ResponseEntity.ok(generateResponseJson("Friend removed", userService.removeFriend(userId, friendId)))
    }

    @PutMapping("/{userId}/add_general_attribute")
    fun addGeneralAttribute(@PathVariable userId: Long, @RequestBody attributeName: String): ResponseEntity<Any> {
        return ResponseEntity.ok(
            generateResponseJson(
                "Attribute $attributeName has been added to all your friends",
                userService.addGeneralAttribute(userId, attributeName)
            )
        )
    }

    @GetMapping("/{userId}/has_general_attribute/{attributeName}")
    fun hasGeneralAttribute(@PathVariable userId: Long, @PathVariable attributeName: String): ResponseEntity<Any> {
        return ResponseEntity.ok(
            generateResponseJson(
                "General attribute check",
                userService.hasGeneralAttribute(userId, attributeName).toString()
            )
        )
    }

    @DeleteMapping("/{userId}/remove_general_attribute")
    fun removeGeneralAttribute(@PathVariable userId: Long, @RequestBody attributeName: String): ResponseEntity<Any> {
        return ResponseEntity.ok(
            generateResponseJson(
                "Attribute $attributeName has been removed from all your friends",
                userService.removeGeneralAttribute(userId, attributeName)
            )
        )
    }
}