package com.klim.trossage_android.data.remote.dto

data class ChatsListResponse(
    val chats: List<ChatResponse>,
    val total: Int,
    val limit: Int,
    val offset: Int
)
