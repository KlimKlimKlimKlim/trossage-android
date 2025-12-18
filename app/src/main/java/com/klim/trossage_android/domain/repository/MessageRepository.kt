package com.klim.trossage_android.domain.repository

import com.klim.trossage_android.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    fun getMessagesFlow(chatId: String): Flow<List<Message>>

    suspend fun loadMessages(chatId: String, offset: Int, limit: Int): Result<List<Message>>

    suspend fun sendMessage(chatId: String, text: String): Result<Message>
}