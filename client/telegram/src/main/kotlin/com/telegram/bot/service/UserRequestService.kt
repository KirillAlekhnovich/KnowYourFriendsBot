package com.telegram.bot.service

import com.beust.klaxon.Klaxon
import com.telegram.bot.dto.FriendDTO
import com.telegram.bot.dto.UserDTO
import com.telegram.bot.utils.HttpRequestBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class UserRequestService(
    private val httpRequestBuilder: HttpRequestBuilder
) {

    @Value("\${backend.url}/users")
    private lateinit var usersEndpoint: String

    private val klaxon = Klaxon()

    fun exists(id: Long): Boolean {
        val url = "$usersEndpoint/exists/$id"
        val response = httpRequestBuilder.get(url)
        return response == "true"
    }

    fun registerUser(id: Long): String {
        val url = "$usersEndpoint/$id"
        return httpRequestBuilder.post(url)
    }

    fun getUser(id: Long): UserDTO {
        val url = "$usersEndpoint/$id"
        val userJson = httpRequestBuilder.get(url)
        return klaxon.parse<UserDTO>(userJson) ?: throw RuntimeException("User not found")
    }

    fun resetUser(id: Long): String {
        val url = "$usersEndpoint/$id/reset"
        return httpRequestBuilder.put(url)
    }

    fun getFriendByName(userId: Long, friendName: String): FriendDTO {
        val url = "$usersEndpoint/$userId/friends/$friendName"
        val friendJson = httpRequestBuilder.get(url)
        return klaxon.parse<FriendDTO>(friendJson) ?: throw RuntimeException("Friend with name $friendName was not found")
    }

    fun addFriend(userId: Long, friendDTO: FriendDTO): String {
        val url = "$usersEndpoint/$userId/add_friend"
        return httpRequestBuilder.put(url, friendDTO)
    }

    fun addGeneralAttribute(userId: Long, attribute: String): String {
        val url = "$usersEndpoint/$userId/add_general_attribute"
        return httpRequestBuilder.put(url, attribute)
    }

    fun removeGeneralAttribute(userId: Long, attribute: String): String {
        val url = "$usersEndpoint/$userId/remove_general_attribute"
        return httpRequestBuilder.delete(url, attribute)
    }

    fun removeFriend(userId: Long, friendId: Long): String {
        val url = "$usersEndpoint/$userId/remove_friend/$friendId"
        return httpRequestBuilder.delete(url)
    }
}