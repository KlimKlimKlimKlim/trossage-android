package com.klim.trossage_android.domain.repository

import com.klim.trossage_android.domain.model.User

interface UserRepository {
    suspend fun getMe(): Result<User>
    fun getCurrentUser(): User?
    suspend fun updateDisplayName(displayName: String): Result<User>
    suspend fun changePassword(oldPassword: String, newPassword: String): Result<Unit>
    suspend fun deleteAccount(password: String): Result<Unit>
    suspend fun searchUsers(query: String, limit: Int, offset: Int): Result<List<User>>
}
