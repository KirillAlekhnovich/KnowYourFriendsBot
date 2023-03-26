package com.telegram.bot.service

import com.telegram.bot.dto.*
import com.telegram.bot.utils.HttpRequestBuilder
import com.telegram.bot.utils.Jedis.getAccessToken
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

/**
 * Class for sending friend-related requests to the server.
 */
@Service
class FriendRequestService(
    private val httpRequestBuilder: HttpRequestBuilder
) {

    @Value("\${backend.url}/friends")
    private lateinit var friendsEndpoint: String

    fun getFriendNames(userId: Long): List<String> {
        val url = "$friendsEndpoint/names"
        return httpRequestBuilder.get(url, getAccessToken(userId)).getListFromData()
    }

    fun getFriendByName(userId: Long, friendName: String): FriendDTO {
        val url = "$friendsEndpoint/search/$friendName"
        return httpRequestBuilder.get(url, getAccessToken(userId)).getObjectFromData()
    }

    fun addFriend(userId: Long, friendDTO: FriendDTO): String {
        val url = "$friendsEndpoint/add"
        return httpRequestBuilder.post(url, friendDTO, getAccessToken(userId)).getMessage()
    }

    fun removeFriend(userId: Long, friendId: Long): String {
        val url = "$friendsEndpoint/$friendId/remove"
        return httpRequestBuilder.delete(url, getAccessToken(userId)).getMessage()
    }

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