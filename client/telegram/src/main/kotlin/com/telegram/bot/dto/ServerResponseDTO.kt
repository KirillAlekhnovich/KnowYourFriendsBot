package com.telegram.bot.dto

import com.beust.klaxon.Klaxon
import java.io.StringReader

data class ResponseDTO(
    var body: String,
    var statusCode: Int,
)

val klaxon = Klaxon()

fun ResponseDTO.getMessage(): String {
    return klaxon.parse<ResponseJsonDTO>(this.body)?.message ?: "Something went wrong"
}

fun ResponseDTO.getData(): String {
    return if (this.statusCode >= 300) this.getMessage()
    else klaxon.parse<ResponseJsonDTO>(this.body)?.data ?: "Something went wrong"
}

inline fun <reified T>ResponseDTO.getObjectFromData(): T {
    if (this.statusCode >= 300) throw RuntimeException(this.getMessage())
    val parsed = klaxon.parseJsonObject(StringReader(this.body))
    val jsonObject = parsed.obj("data")!!
    return klaxon.parseFromJsonObject<T>(jsonObject)
        ?: throw RuntimeException("Something went wrong")
}

inline fun <reified T>ResponseDTO.getListFromData(): List<T> {
    if (this.statusCode >= 300) throw RuntimeException(this.getMessage())
    val parsed = klaxon.parseJsonObject(StringReader(this.body))
    val dataArray = parsed.array<Any>("data")
    val attributes = dataArray?.let { klaxon.parseFromJsonArray<T>(it) }
    return attributes ?: throw RuntimeException("Something went wrong")
}