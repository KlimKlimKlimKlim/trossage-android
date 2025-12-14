package com.klim.trossage_android.domain.repository

import com.klim.trossage_android.domain.model.Chat
import com.klim.trossage_android.domain.model.User
import com.klim.trossage_android.domain.model.Message
import kotlinx.coroutines.flow.Flow

public interface ChatRepository {

    suspend fun getChats(offset: Int, limit: Int = 10): Result<List<Chat>>

    suspend fun searchUsers(query: String): Result<List<User>>

    suspend fun createOrGetChat(userId: String): Result<Chat>

    suspend fun deleteChat(chatId: String): Result<Unit>

    fun observeNewMessages(): Flow<Message>

    fun observeChatUpdates(): Flow<Chat>

    fun connectWebSocket()

    fun disconnectWebSocket()
}