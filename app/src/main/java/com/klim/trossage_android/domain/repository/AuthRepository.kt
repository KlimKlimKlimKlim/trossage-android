package com.klim.trossage_android.domain.repository

import com.klim.trossage_android.domain.model.User

public interface AuthRepository {
    public suspend fun login(username: String, password: String): Result<User>
    public suspend fun register(username: String, password: String, displayName: String): Result<User>

    public fun logout()
    public fun isLoggedIn(): Boolean
    public fun getCurrentUser(): User?
}