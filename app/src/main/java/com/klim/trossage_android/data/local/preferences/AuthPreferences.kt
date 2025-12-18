package com.klim.trossage_android.data.local.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.klim.trossage_android.domain.model.User

class AuthPreferences(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "encrypted_auth_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val KEY_ACCESS = "access_token"
        private const val KEY_REFRESH = "refresh_token"
        private const val KEY_ID = "user_id"
        private const val KEY_LOGIN = "login"
        private const val KEY_DISPLAY = "display_name"
    }

    fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit()
            .putString(KEY_ACCESS, accessToken)
            .putString(KEY_REFRESH, refreshToken)
            .apply()
    }

    fun saveUser(id: Int, login: String, displayName: String) {
        prefs.edit()
            .putInt(KEY_ID, id)
            .putString(KEY_LOGIN, login)
            .putString(KEY_DISPLAY, displayName)
            .apply()
    }

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS, null)

    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH, null)

    fun isLoggedIn(): Boolean = !getAccessToken().isNullOrBlank() && !getRefreshToken().isNullOrBlank()

    fun getCurrentUser(): User? {
        val id = prefs.getInt(KEY_ID, -1)
        if (id == -1) return null
        val login = prefs.getString(KEY_LOGIN, null) ?: return null
        val display = prefs.getString(KEY_DISPLAY, null) ?: return null
        return User(userId = id.toString(), username = login, displayName = display)
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
