package com.klim.trossage_android.data.remote.network

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.klim.trossage_android.data.local.preferences.AuthPreferences
import com.klim.trossage_android.data.remote.dto.ApiResponse
import com.klim.trossage_android.data.remote.dto.TokenResponse
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route

class TokenRefreshAuthenticator(
    private val authPrefs: AuthPreferences,
    private val baseUrl: String,
    private val onSessionExpired: () -> Unit,
    private val gson: Gson = Gson()
) : Authenticator {

    private val lock = Any()
    private val refreshClient = OkHttpClient()

    override fun authenticate(route: Route?, response: Response): Request? {
        // Защита от бесконечного цикла retry
        if (responseCount(response) >= 2) {
            handleSessionExpired()
            return null
        }

        val path = response.request.url.encodedPath
        if (path.endsWith("/auth/refresh") || path.endsWith("/auth/logout")) {
            handleSessionExpired()
            return null
        }

        val currentRefresh = authPrefs.getRefreshToken()
        if (currentRefresh == null) {
            handleSessionExpired()
            return null
        }

        synchronized(lock) {
            // Перепроверяем токен после получения лока
            val freshRefresh = authPrefs.getRefreshToken()
            if (freshRefresh != currentRefresh) {
                // Токен уже обновлён другим потоком, используем новый access token
                val newAccess = authPrefs.getAccessToken()
                return if (newAccess != null) {
                    response.request.newBuilder()
                        .header("Authorization", "Bearer $newAccess")
                        .build()
                } else {
                    null
                }
            }

            // Выполняем refresh
            val refreshRequest = Request.Builder()
                .url(baseUrl.trimEnd('/') + "/auth/refresh")
                .post("".toRequestBody("application/json".toMediaType()))
                .header("Authorization", "Bearer $freshRefresh")
                .build()

            val refreshResp = refreshClient.newCall(refreshRequest).execute()
            if (!refreshResp.isSuccessful) {
                handleSessionExpired()
                return null
            }

            val body = refreshResp.body?.string().orEmpty()
            val type = TypeToken.getParameterized(
                ApiResponse::class.java,
                TokenResponse::class.java
            ).type
            val parsed = gson.fromJson<ApiResponse<TokenResponse>>(body, type)

            if (!parsed.isSuccess || parsed.data == null) {
                handleSessionExpired()
                return null
            }

            authPrefs.saveTokens(parsed.data.accessToken, parsed.data.refreshToken)

            val newAccess = authPrefs.getAccessToken()
            if (newAccess == null) {
                handleSessionExpired()
                return null
            }

            return response.request.newBuilder()
                .header("Authorization", "Bearer $newAccess")
                .build()
        }
    }

    private fun handleSessionExpired() {
        authPrefs.clear()
        onSessionExpired()
    }

    private fun responseCount(response: Response): Int {
        var r: Response? = response
        var count = 1
        while (r?.priorResponse != null) {
            count++
            r = r.priorResponse
        }
        return count
    }
}
