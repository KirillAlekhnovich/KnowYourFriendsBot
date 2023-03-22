package com.telegram.bot.service

import com.telegram.bot.dto.*
import com.telegram.bot.utils.HttpRequestBuilder
import com.telegram.bot.utils.Jedis.getAccessToken
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class FriendRequestService(
    private val httpRequestBuilder: HttpRequestBuilder
) {

    @Value("\${backend.url}/friends")
    private lateinit var friendsEndpoint: String

    fun getFriend(userId: Long, friendId: Long): FriendDTO {
        val url = "$friendsEndpoint/$friendId"
        return httpRequestBuilder.get(url, getAccessToken(userId)).getObjectFromData()
    }

    fun changeFriendsName(userId: Long, friendId: Long, newName: String): String {
        val url = "$friendsEndpoint/$friendId/change_name"
        return httpRequestBuilder.put(url, newName, getAccessToken(userId)).getMessage()
    }

    fun addAttribute(userId: Long, friendId: Long, attribute: AttributeDTO): String {
        val url = "$friendsEndpoint/$friendId/add_attribute"
        return httpRequestBuilder.put(url, attribute, getAccessToken(userId)).getMessage()
    }

    fun getAttributes(userId: Long, friendId: Long): List<AttributeDTO> {
        val url = "$friendsEndpoint/$friendId/attributes"
        return httpRequestBuilder.get(url, getAccessToken(userId)).getListFromData()
    }

    fun getAttributeNames(userId: Long, friendId: Long): List<String> {
        val url = "$friendsEndpoint/$friendId/attribute_names"
        return httpRequestBuilder.get(url, getAccessToken(userId)).getListFromData()
    }

    fun updateAttribute(userId: Long, friendId: Long, attribute: AttributeDTO): String {
        val url = "$friendsEndpoint/$friendId/update_attribute"
        return httpRequestBuilder.put(url, attribute, getAccessToken(userId)).getMessage()
    }

    fun hasAttribute(userId: Long, friendId: Long, attributeName: String): Boolean {
        val url = "$friendsEndpoint/$friendId/has_attribute/$attributeName"
        return httpRequestBuilder.get(url, getAccessToken(userId)).getData() == "true"
    }

    fun deleteAttribute(userId: Long, friendId: Long, attributeName: String): String {
        val url = "$friendsEndpoint/$friendId/delete_attribute"
        return httpRequestBuilder.delete(url, attributeName, getAccessToken(userId)).getMessage()
    }
}