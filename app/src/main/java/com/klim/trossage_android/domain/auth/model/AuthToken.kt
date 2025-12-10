package com.klim.trossage_android.domain.auth.model

data class AuthToken (
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: Long
) {
    fun isExpired(currentTimeMillis: Long = System.currentTimeMillis()): Boolean {
        val bufferMillis = 30_000L
        return currentTimeMillis >= (expiresAt - bufferMillis)
    }
    fun getRemainingTime(currentTimeMillis: Long = System.currentTimeMillis()): Long {
        return expiresAt - currentTimeMillis
    }
}
