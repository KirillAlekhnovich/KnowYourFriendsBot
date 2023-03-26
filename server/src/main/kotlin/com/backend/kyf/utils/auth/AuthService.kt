package com.backend.kyf.utils.auth

import com.backend.kyf.exception.AccessDeniedException
import com.backend.kyf.utils.RedisParams
import com.backend.kyf.utils.auth.Jedis.getValue
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import java.time.Instant

/**
 * Service for handling authorization-related requests.
 */
@Service
class AuthService {
    private val tokenEncryptor = TokenEncryptor()

    /**
     * Gets the id of the user who sent the request.
     */
    fun getAuthorizedUserId(): Long {
        return SecurityContextHolder.getContext().authentication.principal as Long
    }

    /**
     * Checks if the access token is valid.
     */
    fun authorizeUser(receivedAccessToken: String): Long {
        val decryptedToken = tokenEncryptor.decrypt(receivedAccessToken)
        val userId = decryptedToken.split(":")[0].toLongOrNull()
        val accessToken = userId?.let { getValue(userId, RedisParams.ACCESS_TOKEN.name) }
        return if (receivedAccessToken == accessToken) userId else throw AccessDeniedException()
    }

    /**
     * Generates an access token for the user.
     */
    fun generateAccessToken(userId: Long): String {
        val timestamp = Instant.now().epochSecond
        val plaintext = "$userId:$timestamp"
        return tokenEncryptor.encrypt(plaintext)
    }
}
