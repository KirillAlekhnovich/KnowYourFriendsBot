package com.telegram.bot.service

import com.telegram.bot.dto.*
import com.telegram.bot.utils.HttpRequestBuilder
import com.telegram.bot.utils.Jedis.getAccessToken
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

/**
 * Class for sending user-related requests to the server.
 */
@Service
class UserRequestService(
    private val httpRequestBuilder: HttpRequestBuilder
) {

    @Value("\${backend.url}/users")
    private lateinit var usersEndpoint: String

    fun exists(userId: Long): Boolean {
        val url = "$usersEndpoint/exists/$userId"
        return httpRequestBuilder.get(url).getData() == "true"
    }

    fun registerUser(userId: Long): String {
        val url = "$usersEndpoint/$userId"
        return httpRequestBuilder.post(url).getData()
    }

    fun getUser(userId: Long): UserDTO {
        val url = "$usersEndpoint/$userId"
        return httpRequestBuilder.get(url, getAccessToken(userId)).getObjectFromData()
    }

    fun resetUser(userId: Long): String {
        val url = "$usersEndpoint/$userId/reset"
        return httpRequestBuilder.put(url, getAccessToken(userId)).getMessage()
    }

    fun addGeneralAttribute(userId: Long, attribute: String): String {
        val url = "$usersEndpoint/$userId/add_general_attribute"
        return httpRequestBuilder.put(url, attribute, getAccessToken(userId)).getMessage()
    }

    fun removeGeneralAttribute(userId: Long, attribute: String): String {
        val url = "$usersEndpoint/$userId/remove_general_attribute"
        return httpRequestBuilder.delete(url, attribute, getAccessToken(userId)).getMessage()
    }
}