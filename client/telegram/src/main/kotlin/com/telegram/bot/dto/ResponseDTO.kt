package com.telegram.bot.dto

import com.beust.klaxon.Klaxon
import java.io.StringReader

data class ResponseDTO(
    var body: String,
    var statusCode: Int,
)

val klaxon = Klaxon()

fun ResponseDTO.getMessage(): String {
    return klaxon.parse<ResponseJsonDTO>(this.body)?.message ?: "Something went wrong, type /cancel to start over"
}

fun ResponseDTO.getData(): String {
    return if (this.statusCode >= 300) this.getMessage()
    else klaxon.parse<ResponseJsonDTO>(this.body)?.data ?: "Something went wrong, type /cancel to start over"
}

fun ResponseDTO.getUserFromData(): UserDTO {
    if (this.statusCode >= 300) throw RuntimeException(this.getMessage())
    val parsed = klaxon.parseJsonObject(StringReader(this.body))
    val jsonObject = parsed.obj("data")!!
    return klaxon.parseFromJsonObject<UserDTO>(jsonObject) ?: throw RuntimeException("Something went wrong, type /cancel to start over")
}

fun ResponseDTO.getFriendFromData(): FriendDTO {
    if (this.statusCode >= 300) throw RuntimeException(this.getMessage())
    val parsed = klaxon.parseJsonObject(StringReader(this.body))
    val jsonObject = parsed.obj("data")!!
    return klaxon.parseFromJsonObject<FriendDTO>(jsonObject) ?: throw RuntimeException("Something went wrong, type /cancel to start over")
}