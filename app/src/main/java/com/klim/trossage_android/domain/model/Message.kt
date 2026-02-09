package com.klim.trossage_android.domain.model

data class Message(
    val messageId: Int,
    val chatId: Int,
    val senderId: Int,
    val senderDisplayName: String,
    val text: String,
    val timestamp: Long,
    val isMine: Boolean,
    val status: MessageStatus = MessageStatus.SENT
)

enum class MessageStatus {
    SENDING,
    SENT,
    FAILED
}
