package com.backend.kyf.controller

import com.backend.kyf.dto.AttributeDTO
import com.backend.kyf.dto.FriendDTO
import com.backend.kyf.service.FriendService
import com.backend.kyf.utils.ResponseBuilder.buildResponse
import com.backend.kyf.utils.auth.AuthService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Controller for handling friend-related requests.
 */
@RestController
@RequestMapping("/friends")
class FriendController(
    private val friendService: FriendService,
    private val authService: AuthService
) {

    /**
     * Adds a friend to the user's friend list.
     */
    @PostMapping("/add")
    fun addFriend(@RequestBody friendDTO: FriendDTO): ResponseEntity<Any> {
        return ResponseEntity.ok(
            buildResponse(
                "Friend added",
                friendService.addFriend(authService.getAuthorizedUserId(), friendDTO)
            )
        )
    }

    /**
     * Gets friend info by id.
     */
    @GetMapping("/{friendId}")
    fun getFriend(@PathVariable friendId: Long): ResponseEntity<Any> {
        return ResponseEntity.ok(
            buildResponse(
                "Friend retrieved",
                friendService.getFriendDTOById(authService.getAuthorizedUserId(), friendId)
            )
        )
    }

    /**
     * Gets friend info by name.
     */
    @GetMapping("/search/{friendName}")
    fun getFriendByName(@PathVariable friendName: String): ResponseEntity<Any> {
        return ResponseEntity.ok(
            buildResponse(
                "Friend retrieved",
                friendService.getFriendByName(authService.getAuthorizedUserId(), friendName)
            )
        )
    }

    /**
     * Gets all user's friends.
     */
    @GetMapping
    fun getAllFriends(): ResponseEntity<Any> {
        return ResponseEntity.ok(
            buildResponse(
                "List of friends retrieved",
                friendService.getAllFriends(authService.getAuthorizedUserId())
            )
        )
    }

    /**
     * Gets all user's friends' names.
     */
    @GetMapping("/names")
    fun getAllFriendNames(): ResponseEntity<Any> {
        return ResponseEntity.ok(
            buildResponse(
                "List of friend names retrieved",
                friendService.getAllFriendNames(authService.getAuthorizedUserId())
            )
        )
    }

    /**
     * Updates friend.
     */
    @PutMapping("/{friendId}/update")
    fun updateFriend(@PathVariable friendId: Long, @RequestBody newFriendDTO: FriendDTO): ResponseEntity<Any> {
        return ResponseEntity.ok(
            buildResponse(
                "Friend updated",
                friendService.updateFriend(authService.getAuthorizedUserId(), friendId, newFriendDTO)
            )
        )
    }

    /**
     * Changes the name of a friend.
     */
    @PutMapping("/{friendId}/change_name")
    fun changeFriendsName(@PathVariable friendId: Long, @RequestBody newName: String): ResponseEntity<Any> {
        return ResponseEntity.ok(
            buildResponse(
                "Friend's name has been changed to $newName",
                friendService.changeFriendsName(authService.getAuthorizedUserId(), friendId, newName)
            )
        )
    }

    /**
     * Removes a friend from the user's friend list.
     */
    @DeleteMapping("/{friendId}/remove")
    fun removeFriend(@PathVariable friendId: Long): ResponseEntity<Any> {
        return ResponseEntity.ok(
            buildResponse(
                "Friend removed",
                friendService.removeFriend(authService.getAuthorizedUserId(), friendId)
            )
        )
    }

    /**
     * Adds an attribute to a friend.
     */
    @PutMapping("/{friendId}/add_attribute")
    fun addAttribute(@PathVariable friendId: Long, @RequestBody attributeDTO: AttributeDTO): ResponseEntity<Any> {
        return ResponseEntity.ok(
            buildResponse(
                "Attribute added",
                friendService.addAttribute(authService.getAuthorizedUserId(), friendId, attributeDTO)
            )
        )
    }

    /**
     * Checks whether friend has attribute.
     */
    @GetMapping("/{friendId}/has_attribute/{attributeName}")
    fun hasAttribute(@PathVariable friendId: Long, @PathVariable attributeName: String): ResponseEntity<Any> {
        return ResponseEntity.ok(
            buildResponse(
                "Attribute exists check",
                friendService.hasAttribute(authService.getAuthorizedUserId(), friendId, attributeName).toString()
            )
        )
    }

    /**
     * Gets all attributes of a friend.
     */
    @GetMapping("/{friendId}/attributes")
    fun getAttributes(@PathVariable friendId: Long): ResponseEntity<Any> {
        return ResponseEntity.ok(
            buildResponse(
                "Attributes retrieved",
                friendService.getAttributes(authService.getAuthorizedUserId(), friendId)
            )
        )
    }

    /**
     * Gets all attribute names of a friend.
     */
    @GetMapping("/{friendId}/attribute_names")
    fun getAttributeNames(@PathVariable friendId: Long): ResponseEntity<Any> {
        return ResponseEntity.ok(
            buildResponse(
                "Attribute names retrieved",
                friendService.getAttributeNames(authService.getAuthorizedUserId(), friendId)
            )
        )
    }

    /**
     * Updates friend's attribute value.
     */
    @PutMapping("/{friendId}/update_attribute")
    fun updateAttribute(@PathVariable friendId: Long, @RequestBody attributeDTO: AttributeDTO): ResponseEntity<Any> {
        return ResponseEntity.ok(
            buildResponse(
                "Attribute updated",
                friendService.updateAttribute(authService.getAuthorizedUserId(), friendId, attributeDTO)
            )
        )
    }

    /**
     * Removes an attribute from a friend.
     */
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