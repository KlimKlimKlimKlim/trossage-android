// data/repository/ChatRepositoryImpl.kt
package com.klim.trossage_android.data.repository

import com.klim.trossage_android.data.local.preferences.AuthPreferences
import com.klim.trossage_android.data.mapper.ChatMapper
import com.klim.trossage_android.data.remote.api.ChatApiService
import com.klim.trossage_android.data.remote.dto.CreateChatRequest
import com.klim.trossage_android.data.remote.websocket.ChatWebSocketManager
import com.klim.trossage_android.domain.model.Chat
import com.klim.trossage_android.domain.model.Message
import com.klim.trossage_android.domain.model.User
import com.klim.trossage_android.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ChatRepositoryImpl(
    private val api: ChatApiService,
    private val webSocketManager: ChatWebSocketManager,
    private val authPreferences: AuthPreferences
) : ChatRepository {

    override suspend fun getChats(offset: Int, limit: Int): Result<List<Chat>> {
        return try {
            val response = api.getChats(offset, limit)
            val chats = response.chats.map { ChatMapper.toChat(it) }
            Result.success(chats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchUsers(query: String): Result<List<User>> {
        return try {
            val users = api.searchUsers(query).map { ChatMapper.toUser(it) }
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createOrGetChat(userId: String): Result<Chat> {
        return try {
            val response = api.createChat(CreateChatRequest(userId))
            val chat = ChatMapper.toChat(response.chat)
            Result.success(chat)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteChat(chatId: String): Result<Unit> {
        return try {
            api.deleteChat(chatId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeNewMessages(): Flow<Message> {
        val currentUserId = authPreferences.getCurrentUser()?.userId ?: ""
        return webSocketManager.observeMessages()
            .map { dto -> ChatMapper.toMessage(dto, currentUserId) }
    }

    override fun observeChatUpdates(): Flow<Chat> {
        return webSocketManager.observeChatUpdates()
            .map { dto -> ChatMapper.toChat(dto) }
    }

    override fun connectWebSocket() {
    }

    override fun disconnectWebSocket() {
        webSocketManager.disconnect()
    }
}
