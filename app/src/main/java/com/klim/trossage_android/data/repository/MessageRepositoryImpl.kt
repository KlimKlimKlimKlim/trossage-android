package com.klim.trossage_android.data.repository

import com.klim.trossage_android.data.local.preferences.AuthPreferences
import com.klim.trossage_android.data.local.room.dao.MessageDao
import com.klim.trossage_android.data.mapper.ChatMapper
import com.klim.trossage_android.data.remote.api.ChatApiService
import com.klim.trossage_android.data.remote.dto.SendMessageRequest
import com.klim.trossage_android.domain.model.Message
import com.klim.trossage_android.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class MessageRepositoryImpl(
    private val api: ChatApiService,
    private val messageDao: MessageDao,
    private val authPrefs: AuthPreferences
) : MessageRepository {

    private val _messagesFlows = mutableMapOf<Int, MutableStateFlow<List<Message>>>()

    override fun getMessagesFlow(chatId: Int): Flow<List<Message>> {
        return _messagesFlows.getOrPut(chatId) {
            MutableStateFlow(emptyList())
        }
    }

    override suspend fun loadMessages(chatId: Int, offset: Int, limit: Int): Result<List<Message>> {
        return try {
            if (offset == 0) {
                val cached = messageDao.getMessagesByChatId(chatId).map {
                    ChatMapper.fromMessageEntity(it)
                }.sortedBy { it.timestamp }
                if (cached.isNotEmpty()) {
                    _messagesFlows.getOrPut(chatId) { MutableStateFlow(emptyList()) }.value = cached
                }
            }

            val response = api.getMessages(chatId, limit, offset)
            if (!response.isSuccess || response.data == null) {
                return Result.failure(Exception(response.error ?: "Unknown error"))
            }

            val currentUserId = authPrefs.getCurrentUser()?.userId?.toIntOrNull() ?: 0
            val messages = response.data.messages.map { dto ->
                ChatMapper.toMessage(dto, currentUserId, "")
            }.sortedBy { it.timestamp }

            if (offset == 0) {
                messageDao.clearChatMessages(chatId)
                messageDao.insertAll(messages.map { ChatMapper.toMessageEntity(it) })
                messageDao.deleteOldMessages(chatId)
                _messagesFlows.getOrPut(chatId) { MutableStateFlow(emptyList()) }.value = messages
            }

            Result.success(messages)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun sendMessage(chatId: Int, text: String): Result<Message> {
        return try {
            val response = api.sendMessage(chatId, SendMessageRequest(text))
            if (!response.isSuccess || response.data == null) {
                return Result.failure(Exception(response.error ?: "Unknown error"))
            }

            val currentUserId = authPrefs.getCurrentUser()?.userId?.toIntOrNull() ?: 0
            val currentUserName = authPrefs.getCurrentUser()?.displayName ?: ""
            val message = ChatMapper.toMessage(response.data, currentUserId, currentUserName)

            messageDao.insertMessage(ChatMapper.toMessageEntity(message))

            Result.success(message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
