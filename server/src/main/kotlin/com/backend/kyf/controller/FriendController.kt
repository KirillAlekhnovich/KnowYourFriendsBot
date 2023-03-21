package com.backend.kyf.controller

import com.backend.kyf.dto.AttributeDTO
import com.backend.kyf.dto.FriendDTO
import com.backend.kyf.service.FriendService
import com.backend.kyf.utils.auth.AuthService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping("/friends")
class FriendController(
    private val friendService: FriendService,
    private val authService: AuthService
) {

    fun generateResponseJson(message: String, dto: Any): MutableMap<String, Any?> {
        val body: MutableMap<String, Any?> = LinkedHashMap()
        body["timestamp"] = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))
        body["message"] = message
        body["data"] = dto
        return body
    }

    @GetMapping("/{friendId}")
    fun getFriend(@PathVariable friendId: Long): ResponseEntity<Any> {
        return ResponseEntity.ok(generateResponseJson("Friend retrieved", friendService.getFriendDTOById(authService.getAuthorizedUserId(), friendId)))
    }

    @PutMapping("/{friendId}/update")
    fun updateFriend(@PathVariable friendId: Long, @RequestBody newFriendDTO: FriendDTO): ResponseEntity<Any> {
        return ResponseEntity.ok(generateResponseJson("Friend updated", friendService.updateFriend(authService.getAuthorizedUserId(), friendId, newFriendDTO)))
    }

    @DeleteMapping("/{friendId}/delete")
    fun deleteFriend(@PathVariable friendId: Long): ResponseEntity<Any> {
        return ResponseEntity.ok(generateResponseJson("Friend with id $friendId has been deleted", friendService.deleteFriend(authService.getAuthorizedUserId(), friendId)))
    }

    @PutMapping("/{friendId}/add_attribute")
    fun addAttribute(@PathVariable friendId: Long, @RequestBody attributeDTO: AttributeDTO): ResponseEntity<Any> {
        return ResponseEntity.ok(generateResponseJson("Attribute added", friendService.addAttribute(authService.getAuthorizedUserId(), friendId, attributeDTO)))
    }

    @GetMapping("/{friendId}/attributes")
    fun getAttributes(@PathVariable friendId: Long): ResponseEntity<Any> {
        return ResponseEntity.ok(generateResponseJson("Attributes retrieved", friendService.getAttributes(authService.getAuthorizedUserId(), friendId)))
    }

    @GetMapping("/{friendId}/attribute_names")
    fun getAttributeNames(@PathVariable friendId: Long): ResponseEntity<Any> {
        return ResponseEntity.ok(generateResponseJson("Attribute names retrieved", friendService.getAttributeNames(authService.getAuthorizedUserId(), friendId)))
    }

    @PutMapping("/{friendId}/update_attribute")
    fun updateAttribute(@PathVariable friendId: Long, @RequestBody attributeDTO: AttributeDTO): ResponseEntity<Any> {
        return ResponseEntity.ok(generateResponseJson("Attribute updated", friendService.updateAttribute(authService.getAuthorizedUserId(), friendId, attributeDTO)))
    }

    @GetMapping("/{friendId}/has_attribute/{attributeName}")
    fun hasAttribute(@PathVariable friendId: Long, @PathVariable attributeName: String): ResponseEntity<Any> {
        return ResponseEntity.ok(generateResponseJson("Attribute exists check", friendService.hasAttribute(authService.getAuthorizedUserId(), friendId, attributeName).toString()))
    }

    @DeleteMapping("/{friendId}/delete_attribute")
    fun deleteAttribute(@PathVariable friendId: Long, @RequestBody attributeName: String): ResponseEntity<Any> {
        return ResponseEntity.ok(generateResponseJson("Attribute deleted", friendService.deleteAttribute(authService.getAuthorizedUserId(), friendId, attributeName)))
    }
}