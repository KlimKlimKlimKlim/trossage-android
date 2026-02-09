package com.klim.trossage_android.data.remote.network

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.klim.trossage_android.data.local.jwt.JwtParser
import com.klim.trossage_android.data.local.preferences.AuthPreferences
import com.klim.trossage_android.data.remote.dto.ApiResponse
import com.klim.trossage_android.data.remote.dto.TokenResponse
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor

class AuthHeaderInterceptor(
    private val authPrefs: AuthPreferences,
    private val baseUrl: String,
    private val gson: Gson = Gson()
) : Interceptor {

    private val lock = Any()
    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        Log.d("API_LOG", message)
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    private val refreshClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        val authType = original.header(AuthType.HEADER) ?: AuthType.ACCESS

        if (authType == AuthType.REFRESH) {
            val refreshToken = authPrefs.getRefreshToken()
            val builder = original.newBuilder().removeHeader(AuthType.HEADER)
            if (!refreshToken.isNullOrBlank()) {
                builder.header("Authorization", "Bearer $refreshToken")
            }
            return chain.proceed(builder.build())
        }

        val currentAccessToken = authPrefs.getAccessToken()
        if (JwtParser.isTokenExpiringSoon(currentAccessToken, thresholdSeconds = 60)) {
            synchronized(lock) {
                val newAccessToken = authPrefs.getAccessToken()
                if (JwtParser.isTokenExpiringSoon(newAccessToken, thresholdSeconds = 60)) {
                    performTokenRefresh()
                }
            }
        }

        val token = authPrefs.getAccessToken()
        val builder = original.newBuilder().removeHeader(AuthType.HEADER)
        if (!token.isNullOrBlank()) {
            builder.header("Authorization", "Bearer $token")
        }

        return chain.proceed(builder.build())
    }

    private fun performTokenRefresh() {
        val refreshToken = authPrefs.getRefreshToken() ?: return

        try {
            val refreshRequest = Request.Builder()
                .url(baseUrl.trimEnd('/') + "/auth/refresh")
                .post("".toRequestBody("application/json".toMediaType()))
                .header("Authorization", "Bearer $refreshToken")
                .build()

            val response = refreshClient.newCall(refreshRequest).execute()
            if (!response.isSuccessful) return

            val body = response.body?.string().orEmpty()
            val type = TypeToken.getParameterized(
                ApiResponse::class.java,
                TokenResponse::class.java
            ).type
            val parsed = gson.fromJson<ApiResponse<TokenResponse>>(body, type)

            if (parsed.isSuccess && parsed.data != null) {
                authPrefs.saveTokens(parsed.data.accessToken, parsed.data.refreshToken)
            }
        } catch (e: Exception) {
        }
    }
}
