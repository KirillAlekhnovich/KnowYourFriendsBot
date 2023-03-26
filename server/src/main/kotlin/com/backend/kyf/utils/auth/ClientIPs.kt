package com.backend.kyf.utils.auth

/**
 * Class for storing clients' IPs which are allowed to create new users.
 */
object ClientIPs {
    private val allowedIPs: List<String> = System.getenv("AllowedIPs")?.split(",") ?: listOf()

    fun get(): List<String> = allowedIPs
}