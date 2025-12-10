package com.klim.trossage_android.domain.auth.repository

import com.klim.trossage_android.domain.auth.model.AuthToken
import com.klim.trossage_android.domain.auth.model.UserCredentials

interface AuthRepository {
    suspend fun login(credentials: UserCredentials): AuthToken
    suspend fun register(credentials: UserCredentials): AuthToken
    suspend fun refreshToken(): AuthToken
    suspend fun isLoggedIn(): Boolean
    suspend fun getAccessToken(): String?
}