package com.telegram.bot.service

import com.telegram.bot.dto.AttributeDTO
import com.telegram.bot.dto.ResponseDTO
import com.telegram.bot.utils.HttpRequestBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class FriendRequestService(
    private val httpRequestBuilder: HttpRequestBuilder
) {

    @Value("\${backend.url}/friends")
    private lateinit var friendsEndpoint: String

    fun sendRequest(response: ResponseDTO): String {
        if (response.statusCode >= 300) throw RuntimeException("Server returned an error with code: ${response.statusCode}")
        return response.body
    }

    fun addAttribute(friendId: Long, attribute: AttributeDTO): String {
        val url = "$friendsEndpoint/$friendId/add_attribute"
        return sendRequest(httpRequestBuilder.put(url, attribute))
    }

    fun hasAttribute(friendId: Long, attributeName: String): Boolean {
        val url = "$friendsEndpoint/$friendId/has_attribute/$attributeName"
        return sendRequest(httpRequestBuilder.get(url)) == "true"
    }

    fun deleteAttribute(friendId: Long, attributeName: String): String {
        val url = "$friendsEndpoint/$friendId/delete_attribute"
        return sendRequest(httpRequestBuilder.delete(url, attributeName))
    }
}