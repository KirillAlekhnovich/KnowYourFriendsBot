package com.telegram.bot.dto

data class UserDTO(
    var id: Long,
    val friends: MutableSet<FriendDTO>,
    val generalAttributes: MutableSet<String>
)