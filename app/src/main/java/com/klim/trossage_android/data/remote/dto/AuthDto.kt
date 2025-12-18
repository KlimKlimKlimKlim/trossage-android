package com.klim.trossage_android.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LoginUserRequest(
    val login: String,
    val password: String
)

data class RegisterUserRequest(
    val login: String,
    val password: String,
    @SerializedName("display_name") val displayName: String
)

data class TokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String
)

data class UserResponse(
    val id: Int,
    val login: String,
    @SerializedName("display_name") val displayName: String,
    @SerializedName("created_at") val createdAt: String?
)

data class UserAndTokenResponse(
    val user: UserResponse,
    val token: TokenResponse
)