package com.backend.kyf.utils

/**
 * Class for generating server responses.
 */
object ResponseBuilder {
    fun buildResponse(message: String, data: Any): MutableMap<String, Any?> {
        val body: MutableMap<String, Any?> = LinkedHashMap()
        body["message"] = message
        body["data"] = data
        return body
    }
}