package com.backend.kyf.utils.auth

import com.backend.kyf.utils.auth.Jedis.getValue
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class AuthService {
    private val tokenEncryptor = TokenEncryptor()

    fun getAuthorizedUserId(): Long {
        return SecurityContextHolder.getContext().authentication.principal as Long
    }

    fun authorizeUser(receivedAccessToken: String): Long? {
        val decryptedToken = tokenEncryptor.decrypt(receivedAccessToken)
        val userId = decryptedToken.split(":")[0].toLongOrNull()
        val accessToken = userId?.let { getValue(userId, "accessToken") }
        return if (receivedAccessToken == accessToken) userId else null
    }

    fun generateAccessToken(userId: Long): String {
        val timestamp = Instant.now().epochSecond
        val plaintext = "$userId:$timestamp"
        return tokenEncryptor.encrypt(plaintext)
    }
}
