package com.klim.trossage_android.domain.model

data class Chat(
    val chatId: String,
    val companionUserId: String,
    val companionDisplayName: String,
    val lastMessage: String?,
    val lastMessageTimestamp: Long,
    val isRead: Boolean
)