package com.klim.trossage_android.domain.repository

interface SessionRepository {
    suspend fun logoutDevice(): Result<Unit>
    suspend fun logoutAll(): Result<Unit>
}
