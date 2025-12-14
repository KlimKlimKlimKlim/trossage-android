package com.klim.trossage_android.data.repository

import com.klim.trossage_android.data.local.preferences.AuthPreferences
import com.klim.trossage_android.data.remote.api.ChatApiService
import com.klim.trossage_android.data.remote.dto.LoginRequest
import com.klim.trossage_android.data.remote.dto.RegisterRequest
import com.klim.trossage_android.domain.model.User
import com.klim.trossage_android.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val api: ChatApiService,
    private val authPrefs: AuthPreferences
) : AuthRepository {

    override suspend fun login(username: String, password: String): Result<User> {
        return try {
            val response = api.login(LoginRequest(username, password))

            if (!response.isSuccess || response.data == null) {
                return Result.failure(Exception(response.error))
            }

            val data = response.data

            authPrefs.saveAuthData(
                accessToken = data.token.accessToken,
                refreshToken = data.token.refreshToken,
                userId = data.user.id,
                login = data.user.login,
                displayName = data.user.displayName
            )

            val user = User(
                userId = data.user.id.toString(),
                username = data.user.login,
                displayName = data.user.displayName
            )

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(username: String, password: String, displayName: String): Result<User> {
        return try {
            val response = api.register(RegisterRequest(username, password, displayName))

            if (!response.isSuccess || response.data == null) {
                return Result.failure(Exception(response.error))
            }

            val data = response.data

            authPrefs.saveAuthData(
                accessToken = data.token.accessToken,
                refreshToken = data.token.refreshToken,
                userId = data.user.id,
                login = data.user.login,
                displayName = data.user.displayName
            )

            val user = User(
                userId = data.user.id.toString(),
                username = data.user.login,
                displayName = data.user.displayName
            )

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun logout() {
        authPrefs.clear()
    }

    override fun isLoggedIn(): Boolean = authPrefs.isLoggedIn()

    override fun getCurrentUser(): User? = authPrefs.getCurrentUser()
}
