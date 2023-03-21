package com.backend.kyf.dto


data class FriendDTO(
    var id: Long,
    var name: String,
    val ownerId: Long,
    val attributes: MutableMap<String, String?>
)