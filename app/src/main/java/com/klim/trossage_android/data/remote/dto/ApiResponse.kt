package com.klim.trossage_android.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("is_success") val isSuccess: Boolean,
    val error: String?,
    val data: T?
)

typealias LoginResponse = ApiResponse<UserAndTokenResponse>
typealias RegisterResponse = ApiResponse<UserAndTokenResponse>