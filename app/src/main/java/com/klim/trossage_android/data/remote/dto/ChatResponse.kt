package com.klim.trossage_android.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ChatResponse(
    val id: Int,
    @SerializedName("other_user") val otherUser: UserResponse,
    @SerializedName("last_message") val lastMessage: LastMessageInfo?,
    @SerializedName("created_at") val createdAt: String,
    val type: String
)
