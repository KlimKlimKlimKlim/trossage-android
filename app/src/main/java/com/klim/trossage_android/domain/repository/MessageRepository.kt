package com.klim.trossage_android.domain.repository

import com.klim.trossage_android.domain.model.Message
import kotlinx.coroutines.flow.Flow

public interface MessageRepository {

    suspend fun getMessages(chatId: String, offset: Int, limit: Int = 10): Result<List<Message>>

    suspend fun sendMessage(chatId: String, text: String): Result<Message>

    fun observeNewMessages(chatId: String): Flow<Message>

    fun observeTyping(chatId: String): Flow<String>

    // Отправить typing update (что я печатаю)
    fun sendTypingUpdate(chatId: String, text: String)

    // Подключить/отключить WebSocket'ы
    fun connectWebSockets(chatId: String)
    fun disconnectWebSockets()
}