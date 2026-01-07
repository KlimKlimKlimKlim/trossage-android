package com.klim.trossage_android.domain.repository

import com.klim.trossage_android.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    fun getMessagesFlow(chatId: Int): Flow<List<Message>>
    suspend fun loadMessages(chatId: Int, offset: Int, limit: Int): Result<List<Message>>
    suspend fun sendMessage(chatId: Int, text: String): Result<Message>
}
