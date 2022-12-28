package com.backend.kyf.controller

import com.backend.kyf.dto.FriendDTO
import com.backend.kyf.dto.UserDTO
import com.backend.kyf.service.UserService
import org.springframework.beans.factory.annotation.Value
import org.springframework.hateoas.Link
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

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

    @PutMapping("/{userId}/add_friend")
    fun addFriend(@PathVariable userId: Long, @RequestBody friendDTO: FriendDTO): ResponseEntity<UserDTO> {
        val updatedUserDTO: UserDTO = userService.addFriend(userId, friendDTO)
        return ResponseEntity
            .created(Link.of("${baseUrl}/users/${updatedUserDTO.id}").toUri())
            .body(updatedUserDTO)
    }

    @PutMapping("/{userId}/remove_friend")
    fun removeFriend(@PathVariable userId: Long, @RequestBody friendId: Long): ResponseEntity<UserDTO> {
        val updatedUserDTO: UserDTO = userService.removeFriend(userId, friendId)
        return ResponseEntity
            .created(Link.of("${baseUrl}/users/${updatedUserDTO.id}").toUri())
            .body(updatedUserDTO)
    }
}