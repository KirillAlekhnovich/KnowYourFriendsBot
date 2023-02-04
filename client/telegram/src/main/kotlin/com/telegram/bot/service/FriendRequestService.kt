package com.telegram.bot.service

import com.telegram.bot.dto.AttributeDTO
import com.telegram.bot.dto.getData
import com.telegram.bot.dto.getMessage
import com.telegram.bot.utils.HttpRequestBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class FriendRequestService(
    private val httpRequestBuilder: HttpRequestBuilder
) {

    @Value("\${backend.url}/friends")
    private lateinit var friendsEndpoint: String

    fun getFriendInfo(friendId: Long): String {
        val url = "$friendsEndpoint/$friendId/info"
        return httpRequestBuilder.get(url).getData()
    }

    fun addAttribute(friendId: Long, attribute: AttributeDTO): String {
        val url = "$friendsEndpoint/$friendId/add_attribute"
        return httpRequestBuilder.put(url, attribute).getMessage()
    }

    fun updateAttribute(friendId: Long, attribute: AttributeDTO): String {
        val url = "$friendsEndpoint/$friendId/update_attribute"
        return httpRequestBuilder.put(url, attribute).getMessage()
    }

    fun hasAttribute(friendId: Long, attributeName: String): Boolean {
        val url = "$friendsEndpoint/$friendId/has_attribute/$attributeName"
        return httpRequestBuilder.get(url).getData() == "true"
    }

    fun deleteAttribute(friendId: Long, attributeName: String): String {
        val url = "$friendsEndpoint/$friendId/delete_attribute"
        return httpRequestBuilder.delete(url, attributeName).getMessage()
    }
}