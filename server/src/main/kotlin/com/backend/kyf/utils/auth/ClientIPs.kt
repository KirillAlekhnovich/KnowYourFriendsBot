package com.backend.kyf.utils.auth

object ClientIPs {
    private val allowedIPs: List<String> = System.getenv("AllowedIPs")?.split(",") ?: listOf()

    fun get(): List<String> = allowedIPs
}