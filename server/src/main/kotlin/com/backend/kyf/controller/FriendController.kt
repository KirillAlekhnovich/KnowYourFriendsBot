package com.backend.kyf.controller

import com.backend.kyf.dto.AttributeDTO
import com.backend.kyf.dto.FriendDTO
import com.backend.kyf.service.FriendService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@RestController
@RequestMapping("/friends")
class FriendController(
    private val friendService: FriendService,
) {

    fun generateResponseJson(message: String, dto: Any): MutableMap<String, Any?> {
        val body: MutableMap<String, Any?> = LinkedHashMap()
        body["timestamp"] = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))
        body["message"] = message
        body["data"] = dto
        return body
    }

    @PostMapping
    fun createFriend(@RequestBody friendDTO: FriendDTO): ResponseEntity<Any> {
        return ResponseEntity.ok(generateResponseJson("Friend created", friendService.createFriend(friendDTO)))
    }

    @GetMapping("/{friendId}")
    fun getFriend(@PathVariable friendId: Long): ResponseEntity<Any> {
        return ResponseEntity.ok(generateResponseJson("Friend retrieved", friendService.getFriendDTOById(friendId)))
    }

    @GetMapping("/{friendId}/info")
    fun getFriendInfo(@PathVariable friendId: Long): ResponseEntity<Any> {
        return ResponseEntity.ok(generateResponseJson("Friend info retrieved", friendService.getFriendInfo(friendId)))
    }

    @PutMapping("/{friendId}/update")
    fun updateFriend(@PathVariable friendId: Long, @RequestBody newFriendDTO: FriendDTO): ResponseEntity<Any> {
        return ResponseEntity.ok(generateResponseJson("Friend updated", friendService.updateFriend(friendId, newFriendDTO)))
    }

    @DeleteMapping("/{friendId}/delete")
    fun deleteFriend(@PathVariable friendId: Long): ResponseEntity<Any> {
        return ResponseEntity.ok(generateResponseJson("Friend with id $friendId has been deleted", friendService.deleteFriend(friendId)))
    }

    @PutMapping("/{friendId}/add_attribute")
    fun addAttribute(@PathVariable friendId: Long, @RequestBody attributeDTO: AttributeDTO): ResponseEntity<Any> {
        return ResponseEntity.ok(generateResponseJson("Attribute added", friendService.addAttribute(friendId, attributeDTO)))
    }

    @PutMapping("/{friendId}/update_attribute")
    fun updateAttribute(@PathVariable friendId: Long, @RequestBody attributeDTO: AttributeDTO): ResponseEntity<Any> {
        return ResponseEntity.ok(generateResponseJson("Attribute updated", friendService.updateAttribute(friendId, attributeDTO)))
    }

    @GetMapping("/{friendId}/has_attribute/{attributeName}")
    fun hasAttribute(@PathVariable friendId: Long, @PathVariable attributeName: String): ResponseEntity<Any> {
        return ResponseEntity.ok(generateResponseJson("Attribute exists check", friendService.hasAttribute(friendId, attributeName).toString()))
    }

    @DeleteMapping("/{friendId}/delete_attribute")
    fun deleteAttribute(@PathVariable friendId: Long, @RequestBody attributeName: String): ResponseEntity<Any> {
        return ResponseEntity.ok(generateResponseJson("Attribute deleted", friendService.deleteAttribute(friendId, attributeName)))
    }
}