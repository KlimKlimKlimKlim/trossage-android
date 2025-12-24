package com.klim.trossage_android.data.local.jwt

import com.google.gson.Gson
import com.google.gson.JsonObject
import java.util.Base64

object JwtParser {

    fun getExpirationTime(token: String?): Long? {
        if (token.isNullOrBlank()) return null

        return try {
            val parts = token.split(".")
            if (parts.size != 3) return null

            val payload = parts[1]
            val decodedBytes = Base64.getUrlDecoder().decode(payload)
            val json = String(decodedBytes, Charsets.UTF_8)

            val jsonObject = Gson().fromJson(json, JsonObject::class.java)
            jsonObject.get("exp")?.asLong
        } catch (e: Exception) {
            null
        }
    }

    fun isTokenExpiringSoon(token: String?, thresholdSeconds: Long = 60): Boolean {
        val exp = getExpirationTime(token) ?: return true
        val currentTime = System.currentTimeMillis() / 1000
        return (exp - currentTime) <= thresholdSeconds
    }
}
