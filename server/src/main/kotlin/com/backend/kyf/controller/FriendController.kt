package com.backend.kyf.controller

import com.backend.kyf.dto.AttributeDTO
import com.backend.kyf.dto.FriendDTO
import com.backend.kyf.service.FriendService
import com.backend.kyf.utils.ResponseBuilder.buildResponse
import com.backend.kyf.utils.auth.AuthService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/friends")
class FriendController(
    private val friendService: FriendService,
    private val authService: AuthService
) {

    @PostMapping("/add")
    fun addFriend(@RequestBody friendDTO: FriendDTO): ResponseEntity<Any> {
        return ResponseEntity.ok(
            buildResponse(
                "Friend added",
                friendService.addFriend(authService.getAuthorizedUserId(), friendDTO)
            )
        )
    }

    @GetMapping("/{friendId}")
    fun getFriend(@PathVariable friendId: Long): ResponseEntity<Any> {
        return ResponseEntity.ok(
            buildResponse(
                "Friend retrieved",
                friendService.getFriendDTOById(authService.getAuthorizedUserId(), friendId)
            )
        )
    }

    @GetMapping("/search/{friendName}")
    fun getFriendByName(@PathVariable friendName: String): ResponseEntity<Any> {
        return ResponseEntity.ok(
            buildResponse(
                "Friend retrieved",
                friendService.getFriendByName(authService.getAuthorizedUserId(), friendName)
            )
        )
    }

    @GetMapping
    fun getAllFriends(): ResponseEntity<Any> {
        return ResponseEntity.ok(
            buildResponse(
                "List of friends retrieved",
                friendService.getAllFriends(authService.getAuthorizedUserId())
            )
        )
    }

    @GetMapping("/names")
    fun getAllFriendNames(): ResponseEntity<Any> {
        return ResponseEntity.ok(
            buildResponse(
                "List of friend names retrieved",
                friendService.getAllFriendNames(authService.getAuthorizedUserId())
            )
        )
    }

    @PutMapping("/{friendId}/update")
    fun updateFriend(@PathVariable friendId: Long, @RequestBody newFriendDTO: FriendDTO): ResponseEntity<Any> {
        return ResponseEntity.ok(
            buildResponse(
                "Friend updated",
                friendService.updateFriend(authService.getAuthorizedUserId(), friendId, newFriendDTO)
            )
        )
    }

    @PutMapping("/{friendId}/change_name")
    fun changeFriendsName(@PathVariable friendId: Long, @RequestBody newName: String): ResponseEntity<Any> {
        return ResponseEntity.ok(
            buildResponse(
                "Friend's name has been changed to $newName",
                friendService.changeFriendsName(authService.getAuthorizedUserId(), friendId, newName)
            )
        )
    }

    @DeleteMapping("/{friendId}/remove")
    fun removeFriend(@PathVariable friendId: Long): ResponseEntity<Any> {
        return ResponseEntity.ok(
            buildResponse(
                "Friend removed",
                friendService.removeFriend(authService.getAuthorizedUserId(), friendId)
            )
        )
    }

    @PutMapping("/{friendId}/add_attribute")
    fun addAttribute(@PathVariable friendId: Long, @RequestBody attributeDTO: AttributeDTO): ResponseEntity<Any> {
        return ResponseEntity.ok(
            buildResponse(
                "Attribute added",
                friendService.addAttribute(authService.getAuthorizedUserId(), friendId, attributeDTO)
            )
        )
    }

    @GetMapping("/{friendId}/has_attribute/{attributeName}")
    fun hasAttribute(@PathVariable friendId: Long, @PathVariable attributeName: String): ResponseEntity<Any> {
        return ResponseEntity.ok(
            buildResponse(
                "Attribute exists check",
                friendService.hasAttribute(authService.getAuthorizedUserId(), friendId, attributeName).toString()
            )
        )
    }

    @GetMapping("/{friendId}/attributes")
    fun getAttributes(@PathVariable friendId: Long): ResponseEntity<Any> {
        return ResponseEntity.ok(
            buildResponse(
                "Attributes retrieved",
                friendService.getAttributes(authService.getAuthorizedUserId(), friendId)
            )
        )
    }

    @GetMapping("/{friendId}/attribute_names")
    fun getAttributeNames(@PathVariable friendId: Long): ResponseEntity<Any> {
        return ResponseEntity.ok(
            buildResponse(
                "Attribute names retrieved",
                friendService.getAttributeNames(authService.getAuthorizedUserId(), friendId)
            )
        )
    }

    @PutMapping("/{friendId}/update_attribute")
    fun updateAttribute(@PathVariable friendId: Long, @RequestBody attributeDTO: AttributeDTO): ResponseEntity<Any> {
        return ResponseEntity.ok(
            buildResponse(
                "Attribute updated",
                friendService.updateAttribute(authService.getAuthorizedUserId(), friendId, attributeDTO)
            )
        )
    }

    @DeleteMapping("/{friendId}/delete_attribute")
    fun removeAttribute(@PathVariable friendId: Long, @RequestBody attributeName: String): ResponseEntity<Any> {
        return ResponseEntity.ok(
            buildResponse(
                "Attribute deleted",
                friendService.removeAttribute(authService.getAuthorizedUserId(), friendId, attributeName)
            )
        )
    }
}