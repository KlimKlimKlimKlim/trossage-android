package com.klim.trossage_android.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LastMessageInfo(
    val text: String,
    @SerializedName("created_at") val createdAt: String,
    val sender: UserResponse
)
