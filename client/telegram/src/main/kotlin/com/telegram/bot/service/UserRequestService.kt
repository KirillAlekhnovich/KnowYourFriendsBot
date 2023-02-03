package com.telegram.bot.service

import com.beust.klaxon.Klaxon
import com.telegram.bot.dto.FriendDTO
import com.telegram.bot.dto.ResponseDTO
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

    fun sendRequest(response: ResponseDTO): String {
        if (response.statusCode >= 300) throw RuntimeException("Server returned an error with code: ${response.statusCode}")
        return response.body
    }

    fun exists(id: Long): Boolean {
        val url = "$usersEndpoint/exists/$id"
        return sendRequest(httpRequestBuilder.get(url)) == "true"
    }

    fun registerUser(id: Long): String {
        val url = "$usersEndpoint/$id"
        return sendRequest(httpRequestBuilder.post(url))
    }

    fun getUser(id: Long): UserDTO {
        val url = "$usersEndpoint/$id"
        return klaxon.parse<UserDTO>(sendRequest(httpRequestBuilder.get(url))) ?: throw RuntimeException("User not found")
    }

    fun resetUser(id: Long): String {
        val url = "$usersEndpoint/$id/reset"
        return sendRequest(httpRequestBuilder.put(url))
    }

    fun getFriendByName(userId: Long, friendName: String): FriendDTO {
        val url = "$usersEndpoint/$userId/friends/$friendName"
        return klaxon.parse<FriendDTO>(sendRequest(httpRequestBuilder.get(url))) ?: throw RuntimeException("Friend with name $friendName was not found")
    }

    fun addFriend(userId: Long, friendDTO: FriendDTO): String {
        val url = "$usersEndpoint/$userId/add_friend"
        return sendRequest(httpRequestBuilder.put(url, friendDTO))
    }

    fun addGeneralAttribute(userId: Long, attribute: String): String {
        val url = "$usersEndpoint/$userId/add_general_attribute"
        return sendRequest(httpRequestBuilder.put(url, attribute))
    }

    fun hasGeneralAttribute(userId: Long, attribute: String): Boolean {
        val url = "$usersEndpoint/$userId/has_general_attribute/$attribute"
        return sendRequest(httpRequestBuilder.get(url)) == "true"
    }

    fun removeGeneralAttribute(userId: Long, attribute: String): String {
        val url = "$usersEndpoint/$userId/remove_general_attribute"
        return sendRequest(httpRequestBuilder.delete(url, attribute))
    }

    fun removeFriend(userId: Long, friendId: Long): String {
        val url = "$usersEndpoint/$userId/remove_friend/$friendId"
        return sendRequest(httpRequestBuilder.put(url))
    }
}