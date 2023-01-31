package com.backend.kyf.dto

data class UserDTO(
    var id: Long,
    val friends: MutableSet<FriendSlimDTO>?,
    val generalAttributes: MutableSet<String>?
)