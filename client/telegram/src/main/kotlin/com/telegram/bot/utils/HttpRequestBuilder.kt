package com.telegram.bot.utils

import com.beust.klaxon.Klaxon
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.ResponseResultOf
import com.telegram.bot.dto.ResponseDTO
import org.springframework.stereotype.Component
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

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
        return getResponse(manager.request(Method.GET, url)
            .apply { accessToken?.let { header("Authorization", it) } }
            .response())
    }

    /**
     * Sending get request to the server with body.
     */
    fun <T> get(url: String, obj: T, accessToken: String? = null): ResponseDTO {
        val json = if (obj is String) obj else klaxon.toJsonString(obj)
        return getResponse(manager.request(Method.GET, url)
            .body(json)
            .header("Content-Type" to "application/json")
            .apply { accessToken?.let { header("Authorization", it) } }
            .response())
    }

    /**
     * Sending post request to the server.
     */
    fun post(url: String, accessToken: String? = null): ResponseDTO {
        return getResponse(manager.request(Method.POST, url)
            .apply { accessToken?.let { header("Authorization", it) } }
            .response())
    }

    /**
     * Sending post request to the server with body.
     */
    fun <T> post(url: String, obj: T, accessToken: String? = null): ResponseDTO {
        val json = if (obj is String) obj else klaxon.toJsonString(obj)
        return getResponse(manager.request(Method.POST, url)
            .body(json)
            .header("Content-Type" to "application/json")
            .apply { accessToken?.let { header("Authorization", it) } }
            .response())
    }

    /**
     * Sending put request to the server.
     */
    fun put(url: String, accessToken: String? = null): ResponseDTO {
        return getResponse(manager.request(Method.PUT, url)
            .apply { accessToken?.let { header("Authorization", it) } }
            .response())
    }

    /**
     * Sending put request to the server with body.
     */
    fun <T> put(url: String, obj: T, accessToken: String? = null): ResponseDTO {
        val json = if (obj is String) obj else klaxon.toJsonString(obj)
        return getResponse(manager.request(Method.PUT, url)
            .body(json)
            .header("Content-Type" to "application/json")
            .apply { accessToken?.let { header("Authorization", it) } }
            .response())
    }

    /**
     * Sending delete request to the server.
     */
    fun delete(url: String, accessToken: String? = null): ResponseDTO {
        return getResponse(manager.request(Method.DELETE, url)
            .apply { accessToken?.let { header("Authorization", it) } }
            .response())
    }

    /**
     * Sending delete request to the server with body.
     */
    fun <T> delete(url: String, obj: T, accessToken: String? = null): ResponseDTO {
        val json = if (obj is String) obj else klaxon.toJsonString(obj)
        return getResponse(manager.request(Method.DELETE, url)
            .body(json)
            .header("Content-Type" to "application/json")
            .apply { accessToken?.let { header("Authorization", it) } }
            .response())
    }

    /**
     * Fuel manager for sending requests. Allows sending HTTPS requests to servers with self-signed certificates.
     */
    val manager : FuelManager = FuelManager().apply {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun getAcceptedIssuers(): Array<X509Certificate>? = null
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) = Unit
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) = Unit
        })

        socketFactory = SSLContext.getInstance("SSL").apply {
            init(null, trustAllCerts, java.security.SecureRandom())
        }.socketFactory

        hostnameVerifier = HostnameVerifier { _, _ -> true }
    }
}