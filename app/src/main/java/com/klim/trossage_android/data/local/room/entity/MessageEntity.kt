package com.klim.trossage_android.data.local.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey
    val id: Int,
    @ColumnInfo(name = "chat_id")
    val chatId: Int,
    @ColumnInfo(name = "sender_id")
    val senderId: Int,
    @ColumnInfo(name = "sender_name")
    val senderName: String,
    val text: String,
    val timestamp: Long,
    @ColumnInfo(name = "is_mine")
    val isMine: Boolean
)
