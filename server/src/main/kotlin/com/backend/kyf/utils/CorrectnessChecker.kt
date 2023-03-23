package com.backend.kyf.utils

object CorrectnessChecker {
    private val forbiddenCharacters = listOf(
        '/', '\\', '?', '#', '@', '$', '{', '}', '%', '*', ':', '|', '\'', '"', '`', '<', '>', '.', '+', '=', '!'
    )

    fun String.nameIsCorrect(): Boolean {
        return !(this.isEmpty()
                || this.any { it in forbiddenCharacters }
                || this.length > 40)
    }
}