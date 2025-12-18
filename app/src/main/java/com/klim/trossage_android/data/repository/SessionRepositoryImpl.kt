package com.klim.trossage_android.data.repository

import com.klim.trossage_android.data.local.preferences.AuthPreferences
import com.klim.trossage_android.data.remote.api.ChatApiService
import com.klim.trossage_android.domain.repository.SessionRepository

class SessionRepositoryImpl(
    private val api: ChatApiService,
    private val authPrefs: AuthPreferences
) : SessionRepository {

    override suspend fun logoutDevice(): Result<Unit> {
        return try {
            val resp = api.logoutDevice()
            if (!resp.isSuccess) {
                return Result.failure(Exception(resp.error))
            }
            authPrefs.clear()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logoutAll(): Result<Unit> {
        return try {
            val resp = api.logoutAll()
            if (!resp.isSuccess) {
                return Result.failure(Exception(resp.error))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
