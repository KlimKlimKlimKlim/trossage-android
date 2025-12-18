package com.klim.trossage_android.data.repository

import com.klim.trossage_android.data.local.preferences.AuthPreferences
import com.klim.trossage_android.data.remote.api.ChatApiService
import com.klim.trossage_android.data.remote.dto.LoginUserRequest
import com.klim.trossage_android.data.remote.dto.RegisterUserRequest
import com.klim.trossage_android.domain.model.User
import com.klim.trossage_android.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val api: ChatApiService,
    private val authPrefs: AuthPreferences
) : AuthRepository {

    override suspend fun login(username: String, password: String): Result<User> {
        return try {
            val resp = api.login(LoginUserRequest(login = username, password = password))
            if (!resp.isSuccess || resp.data == null) {
                return Result.failure(Exception(resp.error))
            }

            val data = resp.data
            authPrefs.saveTokens(data.token.accessToken, data.token.refreshToken)
            authPrefs.saveUser(data.user.id, data.user.login, data.user.displayName)

            Result.success(User(data.user.id.toString(), data.user.login, data.user.displayName))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(username: String, password: String, displayName: String): Result<User> {
        return try {
            val resp = api.register(RegisterUserRequest(login = username, password = password, displayName = displayName))
            if (!resp.isSuccess || resp.data == null) {
                return Result.failure(Exception(resp.error))
            }

            val data = resp.data
            authPrefs.saveTokens(data.token.accessToken, data.token.refreshToken)
            authPrefs.saveUser(data.user.id, data.user.login, data.user.displayName)

            Result.success(User(data.user.id.toString(), data.user.login, data.user.displayName))
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