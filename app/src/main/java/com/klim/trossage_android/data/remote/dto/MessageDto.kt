package com.klim.trossage_android.data.remote.dto

import com.google.gson.annotations.SerializedName

data class MessageDto(
    @SerializedName("message_id")
    val messageId: String,
    @SerializedName("chat_id")
    val chatId: String,
    @SerializedName("sender_id")
    val senderId: String,
    @SerializedName("sender_display_name")
    val senderDisplayName: String,
    val text: String,
    val timestamp: Long
)
data class SendMessageRequest(
    @SerializedName("chat_id")
    val chatId: String,
    val text: String
)

data class SendMessageResponse(
    val message: MessageDto
)

data class MessageListResponse(
    val messages: List<MessageDto>,
    val total: Int
)

data class TypingMessage(
    @SerializedName("chat_id")
    val chatId: String,
    val text: String
)

