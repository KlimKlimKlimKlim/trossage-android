package com.klim.trossage_android.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("is_success")
    val isSuccess: Boolean,
    val error: String,
    val data: T?
)

data class LoginRequest(
    val login: String,
    val password: String
)

data class RegisterRequest(
    val login: String,
    val password: String,
    @SerializedName("display_name")
    val displayName: String
)

data class AuthData(
    val user: UserDto,
    val token: TokenDto
)

data class UserDto(
    val id: Int,
    val login: String,
    @SerializedName("display_name")
    val displayName: String
)

data class TokenDto(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("refresh_token")
    val refreshToken: String
)

// Для /refresh
data class RefreshTokenRequest(
    @SerializedName("refresh_token")
    val refreshToken: String
)
