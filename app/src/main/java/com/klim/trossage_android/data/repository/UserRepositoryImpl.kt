package com.klim.trossage_android.data.repository

import com.klim.trossage_android.data.local.preferences.AuthPreferences
import com.klim.trossage_android.data.mapper.ChatMapper
import com.klim.trossage_android.data.remote.api.ChatApiService
import com.klim.trossage_android.data.remote.dto.DeleteUserRequest
import com.klim.trossage_android.data.remote.dto.UpdateDisplayNameRequest
import com.klim.trossage_android.data.remote.dto.UpdatePasswordRequest
import com.klim.trossage_android.data.remote.network.ApiErrorHandler
import com.klim.trossage_android.domain.model.User
import com.klim.trossage_android.domain.repository.UserRepository

class UserRepositoryImpl(
    private val api: ChatApiService,
    private val authPrefs: AuthPreferences
) : UserRepository {

    override suspend fun getMe(): Result<User> {
        return try {
            val response = api.getMe()
            if (!response.isSuccess || response.data == null) {
                return Result.failure(Exception(response.error ?: "Ошибка получения профиля"))
            }
            Result.success(ChatMapper.toUser(response.data))
        } catch (e: Exception) {
            Result.failure(Exception(ApiErrorHandler.handleError(e)))
        }
    }

    override suspend fun updateDisplayName(newDisplayName: String): Result<User> {
        return try {
            val response = api.updateDisplayName(UpdateDisplayNameRequest(newDisplayName))
            if (!response.isSuccess || response.data == null) {
                return Result.failure(Exception(response.error ?: "Ошибка обновления имени"))
            }
            val user = response.data.user
            authPrefs.saveUser(user.id, user.login, user.displayName)
            Result.success(User(user.id.toString(), user.login, user.displayName))
        } catch (e: Exception) {
            Result.failure(Exception(ApiErrorHandler.handleError(e)))
        }
    }

    override suspend fun changePassword(oldPassword: String, newPassword: String): Result<Unit> {
        return try {
            val response = api.updatePassword(UpdatePasswordRequest(oldPassword, newPassword))
            if (!response.isSuccess) {
                return Result.failure(Exception(response.error ?: "Ошибка смены пароля"))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(ApiErrorHandler.handleError(e)))
        }
    }

    override suspend fun deleteAccount(password: String): Result<Unit> {
        return try {
            val response = api.deleteMe(DeleteUserRequest(password))
            if (!response.isSuccess) {
                return Result.failure(Exception(response.error ?: "Ошибка удаления аккаунта"))
            }
            authPrefs.clear()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(ApiErrorHandler.handleError(e)))
        }
    }

    override suspend fun searchUsers(query: String, limit: Int, offset: Int): Result<List<User>> {
        return try {
            val response = api.searchUsers(query, limit, offset)
            if (!response.isSuccess || response.data == null) {
                return Result.failure(Exception(response.error ?: "Ошибка поиска"))
            }
            Result.success(response.data.users.map { ChatMapper.toUser(it) })
        } catch (e: Exception) {
            Result.failure(Exception(ApiErrorHandler.handleError(e)))
        }
    }

    override fun getCurrentUser(): User? = authPrefs.getCurrentUser()
}
