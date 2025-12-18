package com.klim.trossage_android.data.repository

import com.klim.trossage_android.data.local.preferences.AuthPreferences
import com.klim.trossage_android.data.remote.api.ChatApiService
import com.klim.trossage_android.data.remote.dto.DeleteUserRequest
import com.klim.trossage_android.data.remote.dto.UpdateDisplayNameRequest
import com.klim.trossage_android.data.remote.dto.UpdatePasswordRequest
import com.klim.trossage_android.domain.model.User
import com.klim.trossage_android.domain.repository.UserRepository

class UserRepositoryImpl(
    private val api: ChatApiService,
    private val authPrefs: AuthPreferences
) : UserRepository {

    override suspend fun getMe(): Result<User> {
        return try {
            val resp = api.getMe()
            if (!resp.isSuccess || resp.data == null) {
                return Result.failure(Exception(resp.error ?: "Unknown error"))
            }
            val u = resp.data
            authPrefs.saveUser(u.id, u.login, u.displayName)
            Result.success(User(u.id.toString(), u.login, u.displayName))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCurrentUser(): User? = authPrefs.getCurrentUser()

    override suspend fun updateDisplayName(displayName: String): Result<User> {
        return try {
            val resp = api.updateDisplayName(UpdateDisplayNameRequest(displayName = displayName))
            if (!resp.isSuccess || resp.data == null) {
                return Result.failure(Exception(resp.error ?: "Unknown error"))
            }
            val u = resp.data.user
            authPrefs.saveUser(u.id, u.login, u.displayName)
            Result.success(User(u.id.toString(), u.login, u.displayName))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun changePassword(oldPassword: String, newPassword: String): Result<Unit> {
        return try {
            val resp = api.updatePassword(
                UpdatePasswordRequest(
                    oldPassword = oldPassword,
                    newPassword = newPassword
                )
            )
            if (!resp.isSuccess) {
                return Result.failure(Exception(resp.error ?: "Unknown error"))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAccount(password: String): Result<Unit> {
        return try {
            val resp = api.deleteMe(DeleteUserRequest(password))
            if (!resp.isSuccess) {
                return Result.failure(Exception(resp.error ?: "Unknown error"))
            }
            authPrefs.clear()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchUsers(query: String, limit: Int, offset: Int): Result<List<User>> {
        return try {
            val resp = api.searchUsers(query, limit, offset)
            if (!resp.isSuccess || resp.data == null) {
                return Result.failure(Exception(resp.error ?: "Unknown error"))
            }
            val users = resp.data.users.map { userDto ->
                User(
                    userId = userDto.id.toString(),
                    username = userDto.login,
                    displayName = userDto.displayName
                )
            }
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
