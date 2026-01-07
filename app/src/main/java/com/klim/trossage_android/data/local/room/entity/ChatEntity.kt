package com.klim.trossage_android.data.local.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey
    val id: Int,
    @ColumnInfo(name = "other_user_id")
    val otherUserId: Int,
    @ColumnInfo(name = "other_user_name")
    val otherUserName: String,
    @ColumnInfo(name = "last_message_text")
    val lastMessageText: String?,
    @ColumnInfo(name = "last_message_sender_name")
    val lastMessageSenderName: String?,
    @ColumnInfo(name = "last_message_timestamp")
    val lastMessageTimestamp: Long,
    @ColumnInfo(name = "created_at")
    val createdAt: Long
)
