package com.klim.trossage_android.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ChatDto(
    @SerializedName("chat_id")
    val chatId: String,
    @SerializedName("companion_user_id")
    val companionUserId: String,
    @SerializedName("companion_display_name")
    val companionDisplayName: String,
    @SerializedName("last_message")
    val lastMessage: String?,
    @SerializedName("last_message_timestamp")
    val lastMessageTimestamp: Long,
    @SerializedName("is_read")
    val isRead: Boolean
)

data class ChatListResponse(
    val chats: List<ChatDto>,
    val total: Int
)

data class CreateChatRequest(
    @SerializedName("companion_user_id")
    val companionUserId: String
)

data class CreateChatResponse(
    val chat: ChatDto
)

data class WebSocketMessage(
    val type: String,
    val data: String
)