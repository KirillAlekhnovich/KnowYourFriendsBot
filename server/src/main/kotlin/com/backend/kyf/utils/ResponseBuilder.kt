package com.backend.kyf.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object ResponseBuilder {
    fun buildResponse(message: String, data: Any): MutableMap<String, Any?> {
        val body: MutableMap<String, Any?> = LinkedHashMap()
        body["message"] = message
        body["data"] = data
        return body
    }
}