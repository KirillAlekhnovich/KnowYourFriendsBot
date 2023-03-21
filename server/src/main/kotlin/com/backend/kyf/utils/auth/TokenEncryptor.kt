package com.backend.kyf.utils.auth

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor

class TokenEncryptor(private val secretKey: String = System.getenv("ServerSecretKey")) {
    private val encryptor = StandardPBEStringEncryptor().apply {
        setPassword(secretKey)
        setAlgorithm("PBEWithMD5AndDES")
    }

    fun encrypt(plaintext: String): String = encryptor.encrypt(plaintext)
    fun decrypt(ciphertext: String): String = encryptor.decrypt(ciphertext)
}
