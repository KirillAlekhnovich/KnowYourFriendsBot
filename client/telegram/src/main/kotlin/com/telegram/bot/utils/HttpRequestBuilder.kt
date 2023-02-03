package com.telegram.bot.utils

import com.github.kittinunf.fuel.httpDelete
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.httpPut
import com.beust.klaxon.Klaxon
import org.springframework.stereotype.Component

@Component
class HttpRequestBuilder {

    private val klaxon = Klaxon()

    fun get(url: String): String {
        return url.httpGet().response().second.data.toString(Charsets.UTF_8)
    }

    fun post(url: String): String {
        return url.httpPost().response().second.data.toString(Charsets.UTF_8)
    }

    fun <T>post(url: String, obj: T): String {
        val json = klaxon.toJsonString(obj)
        val request = url.httpPost().body(json).header("Content-Type" to "application/json")
        return request.response().second.data.toString(Charsets.UTF_8)
    }

    fun put(url: String): String {
        return url.httpPut().response().second.data.toString(Charsets.UTF_8)
    }

    fun <T>put(url: String, obj: T): String {
        val json = if (obj is String) obj
        else klaxon.toJsonString(obj)
        val request = url.httpPut().body(json).header("Content-Type" to "application/json")
        return request.response().second.data.toString(Charsets.UTF_8)
    }

    fun delete(url: String): String {
        return url.httpDelete().response().second.data.toString(Charsets.UTF_8)
    }

    fun <T>delete(url: String, obj: T): String {
        val json = klaxon.toJsonString(obj)
        val request = url.httpDelete().body(json).header("Content-Type" to "application/json")
        return request.response().second.data.toString(Charsets.UTF_8)
    }
}