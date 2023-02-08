package com.telegram.bot.service

import com.telegram.bot.dto.*
import com.telegram.bot.utils.HttpRequestBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class UserRequestService(
    private val httpRequestBuilder: HttpRequestBuilder
) {

    @Value("\${backend.url}/users")
    private lateinit var usersEndpoint: String


    fun exists(id: Long): Boolean {
        val url = "$usersEndpoint/exists/$id"
        return httpRequestBuilder.get(url).getData() == "true"
    }

    fun registerUser(id: Long): String {
        val url = "$usersEndpoint/$id"
        return httpRequestBuilder.post(url).getData()
    }

    fun getUser(id: Long): UserDTO {
        val url = "$usersEndpoint/$id"
        return httpRequestBuilder.get(url).getObjectFromData()
    }

    fun resetUser(id: Long): String {
        val url = "$usersEndpoint/$id/reset"
        return httpRequestBuilder.put(url).getMessage()
    }

    fun getFriendNames(userId: Long): List<String> {
        val url = "$usersEndpoint/$userId/friends/names"
        return httpRequestBuilder.get(url).getListFromData()
    }

    fun getFriendByName(userId: Long, friendName: String): FriendDTO {
        val url = "$usersEndpoint/$userId/friends/$friendName"
        return httpRequestBuilder.get(url).getObjectFromData()
    }

    fun addFriend(userId: Long, friendDTO: FriendDTO): String {
        val url = "$usersEndpoint/$userId/add_friend"
        return httpRequestBuilder.put(url, friendDTO).getMessage()
    }

    fun addGeneralAttribute(userId: Long, attribute: String): String {
        val url = "$usersEndpoint/$userId/add_general_attribute"
        return httpRequestBuilder.put(url, attribute).getMessage()
    }

    fun hasGeneralAttribute(userId: Long, attribute: String): Boolean {
        val url = "$usersEndpoint/$userId/has_general_attribute/$attribute"
        return httpRequestBuilder.get(url).getData() == "true"
    }

    fun removeGeneralAttribute(userId: Long, attribute: String): String {
        val url = "$usersEndpoint/$userId/remove_general_attribute"
        return httpRequestBuilder.delete(url, attribute).getMessage()
    }

    fun removeFriend(userId: Long, friendId: Long): String {
        val url = "$usersEndpoint/$userId/remove_friend/$friendId"
        return httpRequestBuilder.put(url).getMessage()
    }
}