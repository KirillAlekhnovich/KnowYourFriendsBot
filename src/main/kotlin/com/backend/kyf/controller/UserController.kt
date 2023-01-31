package com.backend.kyf.controller

import com.backend.kyf.dto.FriendDTO
import com.backend.kyf.dto.UserDTO
import com.backend.kyf.service.UserService
import org.springframework.beans.factory.annotation.Value
import org.springframework.hateoas.Link
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService
) {

    @Value("\${server.url}")
    private lateinit var baseUrl: String

    @PostMapping("/{userId}")
    fun createUser(@PathVariable userId: Long): ResponseEntity<UserDTO> {
        val createdUserDTO: UserDTO = userService.registerUser(userId)
        return ResponseEntity
            .created(Link.of("${baseUrl}/users/${createdUserDTO.id}").toUri())
            .body(createdUserDTO)
    }

    @PutMapping("/{userId}/reset")
    fun resetUser(@PathVariable userId: Long): ResponseEntity<Any> {
        userService.reset(userId)
        return ResponseEntity.ok("Your profile has been reset")
    }

    @PutMapping("/{userId}/add_friend")
    fun addFriend(@PathVariable userId: Long, @RequestBody friendDTO: FriendDTO): ResponseEntity<UserDTO> {
        val updatedUserDTO: UserDTO = userService.addFriend(userId, friendDTO)
        return ResponseEntity
            .created(Link.of("${baseUrl}/users/${updatedUserDTO.id}").toUri())
            .body(updatedUserDTO)
    }

    @GetMapping("/{userId}/friends")
    fun getAllFriends(@PathVariable userId: Long): ResponseEntity<List<FriendDTO>> {
        return ResponseEntity.ok(userService.getAllFriends(userId))
    }

    @PutMapping("/{userId}/remove_friend")
    fun removeFriend(@PathVariable userId: Long, @RequestBody friendId: Long): ResponseEntity<UserDTO> {
        val updatedUserDTO: UserDTO = userService.removeFriend(userId, friendId)
        return ResponseEntity
            .created(Link.of("${baseUrl}/users/${updatedUserDTO.id}").toUri())
            .body(updatedUserDTO)
    }

    @PutMapping("/{userId}/add_attribute")
    fun addAttributeToAllFriends(@PathVariable userId: Long, @RequestBody attributeName: String): ResponseEntity<Any> {
        userService.addAttributeToAllFriends(userId, attributeName)
        return ResponseEntity.ok("Attribute $attributeName has been added to all your friends")
    }

    @DeleteMapping("/{userId}/remove_attribute")
    fun removeAttributeFromAllFriends(@PathVariable userId: Long, @RequestBody attributeName: String): ResponseEntity<Any> {
        userService.removeAttributeFromAllFriends(userId, attributeName)
        return ResponseEntity.ok("Attribute $attributeName has been removed from all your friends")
    }
}