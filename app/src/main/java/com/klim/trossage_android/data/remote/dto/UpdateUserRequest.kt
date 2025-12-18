package com.klim.trossage_android.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UpdateDisplayNameRequest(
    @SerializedName("display_name") val displayName: String
)

data class UpdatePasswordRequest(
    @SerializedName("old_password") val oldPassword: String,
    @SerializedName("new_password") val newPassword: String
)

data class DeleteUserRequest(
    @SerializedName("password") val password: String
)

data class UpdateUserResponse(
    val user: UserResponse,
    @SerializedName("tokens_revoked") val tokensRevoked: Boolean,
    @SerializedName("tokens_revoked_reason") val tokensRevokedReason: String?
)
