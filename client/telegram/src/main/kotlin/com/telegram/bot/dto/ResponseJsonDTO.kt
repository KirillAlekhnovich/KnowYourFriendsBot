package com.telegram.bot.dto

class ResponseJsonDTO(
    val timestamp: String,
    val message: String,
    var data: String = ""
)