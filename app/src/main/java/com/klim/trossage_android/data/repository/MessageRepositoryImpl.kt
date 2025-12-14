package com.klim.trossage_android.data.repository

import com.klim.trossage_android.data.local.preferences.AuthPreferences
import com.klim.trossage_android.data.mapper.ChatMapper
import com.klim.trossage_android.data.remote.api.ChatApiService
import com.klim.trossage_android.data.remote.dto.SendMessageRequest
import com.klim.trossage_android.data.remote.websocket.ChatWebSocketManager
import com.klim.trossage_android.data.remote.websocket.TypingWebSocketManager
import com.klim.trossage_android.domain.model.Message
import com.klim.trossage_android.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

class MessageRepositoryImpl(
    private val api: ChatApiService,
    private val chatWebSocketManager: ChatWebSocketManager,
    private val typingWebSocketManager: TypingWebSocketManager,
    private val authPreferences: AuthPreferences
) : MessageRepository {

    override suspend fun getMessages(chatId: String, offset: Int, limit: Int): Result<List<Message>> {
        return try {
            val response = api.getMessages(chatId, offset, limit)
            val currentUserId = authPreferences.getCurrentUser()?.userId ?: ""
            val messages = response.messages.map { dto ->
                ChatMapper.toMessage(dto, currentUserId)
            }
            Result.success(messages)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendMessage(chatId: String, text: String): Result<Message> {
        return try {
            val response = api.sendMessage(SendMessageRequest(chatId, text))
            val currentUserId = authPreferences.getCurrentUser()?.userId ?: ""
            val message = ChatMapper.toMessage(response.message, currentUserId)
            Result.success(message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeNewMessages(chatId: String): Flow<Message> {
        val currentUserId = authPreferences.getCurrentUser()?.userId ?: ""
        return chatWebSocketManager.observeMessages()
            .map { dto -> ChatMapper.toMessage(dto, currentUserId) }
            .filter { message -> message.chatId == chatId }
    }

    override fun observeTyping(chatId: String): Flow<String> {
        return typingWebSocketManager.observeTyping(chatId)
    }

    override fun sendTypingUpdate(chatId: String, text: String) {
        typingWebSocketManager.sendTyping(chatId, text)
    }

    override fun connectWebSockets(chatId: String) {
        // WebSocket'ы подключаются автоматически при подписке на Flow
        // Но можно добавить явное подключение если нужно
    }

    override fun disconnectWebSockets() {
        typingWebSocketManager.disconnect()
        // chatWebSocketManager.disconnect() - оставляем активным для других чатов
    }
}