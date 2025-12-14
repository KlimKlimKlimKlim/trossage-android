package com.klim.trossage_android.domain.model

data class Message(
    val messageId: String,
    val chatId: String,
    val senderId: String,
    val senderDisplayName: String,
    val text: String,
    val timestamp: Long,
    val isMine: Boolean
)