package com.klim.trossage_android.data.remote.dto

data class UsersSearchResponse(
    val users: List<UserResponse>,
    val total: Int,
    val limit: Int,
    val offset: Int
)