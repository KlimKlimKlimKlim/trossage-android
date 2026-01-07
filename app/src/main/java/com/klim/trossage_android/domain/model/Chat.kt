package com.klim.trossage_android.domain.model

data class Chat(
    val chatId: Int,
    val companionUserId: Int,
    val companionDisplayName: String,
    val lastMessage: String?,
    val lastMessageSenderName: String?,
    val lastMessageTimestamp: Long,
    val createdAt: Long
)
