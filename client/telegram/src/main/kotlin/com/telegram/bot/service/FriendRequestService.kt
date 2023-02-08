package com.telegram.bot.service

import com.telegram.bot.dto.*
import com.telegram.bot.utils.HttpRequestBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class FriendRequestService(
    private val httpRequestBuilder: HttpRequestBuilder
) {

    @Value("\${backend.url}/friends")
    private lateinit var friendsEndpoint: String

    fun getFriend(friendId: Long): FriendDTO {
        val url = "$friendsEndpoint/$friendId"
        return httpRequestBuilder.get(url).getObjectFromData()
    }

    fun addAttribute(friendId: Long, attribute: AttributeDTO): String {
        val url = "$friendsEndpoint/$friendId/add_attribute"
        return httpRequestBuilder.put(url, attribute).getMessage()
    }

    fun getAttributes(friendId: Long): List<AttributeDTO> {
        val url = "$friendsEndpoint/$friendId/attributes"
        return httpRequestBuilder.get(url).getListFromData()
    }

    fun getAttributeNames(friendId: Long): List<String> {
        val url = "$friendsEndpoint/$friendId/attribute_names"
        return httpRequestBuilder.get(url).getListFromData()
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