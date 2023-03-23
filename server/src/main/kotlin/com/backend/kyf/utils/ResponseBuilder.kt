package com.backend.kyf.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object ResponseBuilder {
    fun buildResponse(message: String, data: Any): MutableMap<String, Any?> {
        val body: MutableMap<String, Any?> = LinkedHashMap()
        body["timestamp"] = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))
        body["message"] = message
        body["data"] = data
        return body
    }
}