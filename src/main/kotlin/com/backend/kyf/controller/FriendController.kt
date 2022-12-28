package com.backend.kyf.controller

import com.backend.kyf.dto.FriendDTO
import com.backend.kyf.service.FriendService
import org.springframework.beans.factory.annotation.Value
import org.springframework.hateoas.Link
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/friends")
class FriendController(
    private val friendService: FriendService,
) {

    @Value("\${server.url}")
    private lateinit var baseUrl: String

    @PostMapping()
    fun createFriend(@RequestBody friendDTO: FriendDTO): ResponseEntity<FriendDTO> {
        val createdFriendDTO: FriendDTO = friendService.createFriend(friendDTO)
        return ResponseEntity
            .created(Link.of("${baseUrl}/friends/${createdFriendDTO.id}").toUri())
            .body(createdFriendDTO)
    }

    @GetMapping("/{friendId}")
    fun getFriend(@PathVariable friendId: Long): ResponseEntity<FriendDTO> {
        return ResponseEntity.ok(friendService.getFriendById(friendId))
    }

    @GetMapping()
    fun getAllFriends(): ResponseEntity<List<FriendDTO>> {
        return ResponseEntity.ok(friendService.getAllFriends())
    }

    @PutMapping("/{friendId}/update")
    fun updateFriend(@PathVariable friendId: Long, @RequestBody newFriendDTO: FriendDTO): ResponseEntity<FriendDTO> {
        val updatedFriendDTO: FriendDTO = friendService.updateFriend(friendId, newFriendDTO)
        return ResponseEntity
            .created(Link.of("${baseUrl}/friends/${updatedFriendDTO.id}").toUri())
            .body(updatedFriendDTO)
    }

    @DeleteMapping("/{friendId}/delete")
    fun deleteFriend(@PathVariable friendId: Long): ResponseEntity<Any> {
        friendService.deleteFriend(friendId)
        return ResponseEntity.ok("Friend with id $friendId has been deleted")
    }
}