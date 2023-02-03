package com.telegram.bot.dto

data class FriendDTO(
    var id: Long,
    var name: String,
    val attributes: MutableMap<String, String?>
)