package com.backend.kyf.utils

object CorrectnessChecker {
    fun String.isCorrect(): Boolean {
        val forbiddenCharacters = listOf(
            '/', '\\', '?', '#', '@', '$', '{', '}', '%', '*', ':', '|', '\'', '"', '`', '<', '>', '.', '+', '=', '!')
        return !(this.isEmpty()
                || this.any { it in forbiddenCharacters }
                || this.length > 40)
    }
}