package com.klim.trossage_android.data.remote.dto

import com.google.gson.annotations.SerializedName

data class MessageResponse(
    val id: Int,
    @SerializedName("chat_id") val chatId: Int,
    @SerializedName("sender_id") val senderId: Int,
    val text: String,
    @SerializedName("created_at") val createdAt: String
)

data class MessagesResponse(
    val messages: List<MessageResponse>,
    val total: Int,
    val limit: Int,
    val offset: Int
)
