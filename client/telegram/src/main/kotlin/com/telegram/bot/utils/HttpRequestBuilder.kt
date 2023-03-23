package com.telegram.bot.utils

import com.github.kittinunf.fuel.httpDelete
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.httpPut
import com.beust.klaxon.Klaxon
import com.github.kittinunf.fuel.core.ResponseResultOf
import com.telegram.bot.dto.ResponseDTO
import org.springframework.stereotype.Component

@Component
class HttpRequestBuilder {

    private val klaxon = Klaxon()

    fun getResponse(serverResponse: ResponseResultOf<ByteArray>): ResponseDTO {
        return ResponseDTO(
            serverResponse.second.data.toString(Charsets.UTF_8),
            serverResponse.second.statusCode
        )
    }

    fun get(url: String, accessToken: String? = null): ResponseDTO {
        return getResponse(url.httpGet()
            .apply { accessToken?.let { header("Authorization", it) } }
            .response())
    }

    fun <T> get(url: String, obj: T, accessToken: String? = null): ResponseDTO {
        val json = if (obj is String) obj else klaxon.toJsonString(obj)
        return getResponse(url.httpGet()
            .body(json)
            .header("Content-Type" to "application/json")
            .apply { accessToken?.let { header("Authorization", it) } }
            .response())
    }

    fun post(url: String, accessToken: String? = null): ResponseDTO {
        return getResponse(url.httpPost()
            .apply { accessToken?.let { header("Authorization", it) } }
            .response())
    }

    fun <T> post(url: String, obj: T, accessToken: String? = null): ResponseDTO {
        val json = if (obj is String) obj else klaxon.toJsonString(obj)
        return getResponse(url.httpPost()
            .body(json)
            .header("Content-Type" to "application/json")
            .apply { accessToken?.let { header("Authorization", it) } }
            .response())
    }

    fun put(url: String, accessToken: String? = null): ResponseDTO {
        return getResponse(url.httpPut()
            .apply { accessToken?.let { header("Authorization", it) } }
            .response())
    }

    fun <T> put(url: String, obj: T, accessToken: String? = null): ResponseDTO {
        val json = if (obj is String) obj else klaxon.toJsonString(obj)
        return getResponse(url.httpPut()
            .body(json)
            .header("Content-Type" to "application/json")
            .apply { accessToken?.let { header("Authorization", it) } }
            .response())
    }

    fun delete(url: String, accessToken: String? = null): ResponseDTO {
        return getResponse(url.httpDelete()
            .apply { accessToken?.let { header("Authorization", it) } }
            .response())
    }

    fun <T> delete(url: String, obj: T, accessToken: String? = null): ResponseDTO {
        val json = if (obj is String) obj else klaxon.toJsonString(obj)
        return getResponse(url.httpDelete()
            .body(json)
            .header("Content-Type" to "application/json")
            .apply { accessToken?.let { header("Authorization", it) } }
            .response())
    }
}