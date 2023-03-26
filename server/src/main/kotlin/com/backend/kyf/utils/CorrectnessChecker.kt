package com.backend.kyf.utils

/**
 * Class for checking string correctness.
 */
object CorrectnessChecker {
    private val forbiddenCharacters = listOf(
        '/', '\\', '?', '#', '@', '$', '{', '}', '%', '*', ':', '|', '\'', '"', '`', '<', '>', '.', '+', '=', '!'
    )

    /**
     * Checks if friend or attribute name is correct.
     */
    fun String.nameIsCorrect(): Boolean {
        return !(this.isEmpty()
                || this.any { it in forbiddenCharacters }
                || this.length > 40)
    }
}