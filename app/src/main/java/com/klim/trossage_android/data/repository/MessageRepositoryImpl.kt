package com.klim.trossage_android.data.repository

import android.util.Log
import com.google.gson.Gson
import com.klim.trossage_android.data.local.preferences.AuthPreferences
import com.klim.trossage_android.data.local.room.dao.MessageDao
import com.klim.trossage_android.data.mapper.ChatMapper
import com.klim.trossage_android.data.remote.api.ChatApiService
import com.klim.trossage_android.data.remote.dto.SendMessageRequest
import com.klim.trossage_android.data.remote.dto.TypingOperationDto
import com.klim.trossage_android.data.remote.dto.TypingUpdateRequest
import com.klim.trossage_android.domain.model.Message
import com.klim.trossage_android.domain.model.MessageStatus
import com.klim.trossage_android.domain.repository.MessageRepository
import com.klim.trossage_android.domain.repository.TypingOperation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.random.Random

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

    override suspend fun loadMessages(chatId: Int, offset: Int, limit: Int, companionName: String): Result<List<Message>> {
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
            val currentUserName = authPrefs.getCurrentUser()?.displayName ?: ""

            val messages = response.data.messages.map { dto ->
                val senderName = if (dto.senderId == currentUserId) {
                    currentUserName
                } else {
                    companionName
                }
                ChatMapper.toMessage(dto, currentUserId, senderName)
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
        val currentUserId = authPrefs.getCurrentUser()?.userId?.toIntOrNull() ?: 0
        val currentUserName = authPrefs.getCurrentUser()?.displayName ?: ""

        val tempMessage = Message(
            messageId = Random.nextInt(Int.MIN_VALUE, -1),
            chatId = chatId,
            senderId = currentUserId,
            senderDisplayName = currentUserName,
            text = text,
            timestamp = System.currentTimeMillis(),
            isMine = true,
            status = MessageStatus.SENDING
        )

        return try {
            val response = api.sendMessage(chatId, SendMessageRequest(text))
            if (!response.isSuccess || response.data == null) {
                return Result.failure(Exception(response.error ?: "Unknown error"))
            }

            val message = ChatMapper.toMessage(response.data, currentUserId, currentUserName)
            messageDao.insertMessage(ChatMapper.toMessageEntity(message))

            Result.success(message)
        } catch (e: Exception) {
            val failedMessage = tempMessage.copy(status = MessageStatus.FAILED)
            Result.failure(e)
        }
    }

    override suspend fun sendTyping(chatId: Int, operations: List<TypingOperation>) {
        try {
            val request = TypingUpdateRequest(
                operations = operations.map {
                    TypingOperationDto(
                        type = it.type,
                        position = it.position,
                        length = it.length,
                        text = it.text
                    )
                }
            )

            Log.d("REPO", "===== SENDING TYPING =====")
            Log.d("REPO", "chatId: $chatId")
            Log.d("REPO", "Request JSON: ${Gson().toJson(request)}")

            api.sendTyping(chatId, request)

            Log.d("REPO", "Typing sent successfully")
            Log.d("REPO", "==========================")
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("REPO", "===== TYPING ERROR =====")
            Log.e("REPO", "Error: ${e.message}", e)
            Log.e("REPO", "========================")
        }
    }
}
