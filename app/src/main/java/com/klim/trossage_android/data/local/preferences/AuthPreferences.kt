package com.klim.trossage_android.data.local.preferences

import android.content.Context
import android.content.SharedPreferences
import com.klim.trossage_android.domain.model.User

class AuthPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_LOGIN = "login"
        private const val KEY_DISPLAY_NAME = "display_name"
    }

    fun saveAuthData(
        accessToken: String,
        refreshToken: String,
        userId: Int,
        login: String,
        displayName: String
    ) {
        prefs.edit().apply {
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            putInt(KEY_USER_ID, userId)
            putString(KEY_LOGIN, login)
            putString(KEY_DISPLAY_NAME, displayName)
            apply()
        }
    }

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    fun getCurrentUser(): User? {
        val userId = prefs.getInt(KEY_USER_ID, -1)
        if (userId == -1) return null

        val login = prefs.getString(KEY_LOGIN, null) ?: return null
        val displayName = prefs.getString(KEY_DISPLAY_NAME, null) ?: return null

        return User(userId.toString(), login, displayName)
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean = getAccessToken() != null
}
