package com.telegram.bot.utils

import com.github.kittinunf.fuel.httpDelete
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.httpPut
import com.beust.klaxon.Klaxon
import com.github.kittinunf.fuel.core.ResponseResultOf
import com.telegram.bot.dto.ResponseDTO
import org.springframework.stereotype.Component

/**
 * Class that provides methods for sending requests to the server.
 */
@Component
class HttpRequestBuilder {

    private val klaxon = Klaxon()

    /**
     * Getting response from server.
     */
    fun getResponse(serverResponse: ResponseResultOf<ByteArray>): ResponseDTO {
        return ResponseDTO(
            serverResponse.second.data.toString(Charsets.UTF_8),
            serverResponse.second.statusCode
        )
    }

    /**
     * Sending get request to the server.
     */
    fun get(url: String, accessToken: String? = null): ResponseDTO {
        return getResponse(url.httpGet()
            .apply { accessToken?.let { header("Authorization", it) } }
            .response())
    }

    /**
     * Sending get request to the server with body.
     */
    fun <T> get(url: String, obj: T, accessToken: String? = null): ResponseDTO {
        val json = if (obj is String) obj else klaxon.toJsonString(obj)
        return getResponse(url.httpGet()
            .body(json)
            .header("Content-Type" to "application/json")
            .apply { accessToken?.let { header("Authorization", it) } }
            .response())
    }

    /**
     * Sending post request to the server.
     */
    fun post(url: String, accessToken: String? = null): ResponseDTO {
        return getResponse(url.httpPost()
            .apply { accessToken?.let { header("Authorization", it) } }
            .response())
    }

    /**
     * Sending post request to the server with body.
     */
    fun <T> post(url: String, obj: T, accessToken: String? = null): ResponseDTO {
        val json = if (obj is String) obj else klaxon.toJsonString(obj)
        return getResponse(url.httpPost()
            .body(json)
            .header("Content-Type" to "application/json")
            .apply { accessToken?.let { header("Authorization", it) } }
            .response())
    }

    /**
     * Sending put request to the server.
     */
    fun put(url: String, accessToken: String? = null): ResponseDTO {
        return getResponse(url.httpPut()
            .apply { accessToken?.let { header("Authorization", it) } }
            .response())
    }

    /**
     * Sending put request to the server with body.
     */
    fun <T> put(url: String, obj: T, accessToken: String? = null): ResponseDTO {
        val json = if (obj is String) obj else klaxon.toJsonString(obj)
        return getResponse(url.httpPut()
            .body(json)
            .header("Content-Type" to "application/json")
            .apply { accessToken?.let { header("Authorization", it) } }
            .response())
    }

    /**
     * Sending delete request to the server.
     */
    fun delete(url: String, accessToken: String? = null): ResponseDTO {
        return getResponse(url.httpDelete()
            .apply { accessToken?.let { header("Authorization", it) } }
            .response())
    }

    /**
     * Sending delete request to the server with body.
     */
    fun <T> delete(url: String, obj: T, accessToken: String? = null): ResponseDTO {
        val json = if (obj is String) obj else klaxon.toJsonString(obj)
        return getResponse(url.httpDelete()
            .body(json)
            .header("Content-Type" to "application/json")
            .apply { accessToken?.let { header("Authorization", it) } }
            .response())
    }
}