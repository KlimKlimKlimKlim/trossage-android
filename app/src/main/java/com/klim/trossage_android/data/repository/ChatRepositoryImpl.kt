package com.klim.trossage_android.data.repository

import com.klim.trossage_android.data.local.room.dao.ChatDao
import com.klim.trossage_android.data.mapper.ChatMapper
import com.klim.trossage_android.data.remote.api.ChatApiService
import com.klim.trossage_android.data.remote.dto.CreateChatRequest
import com.klim.trossage_android.data.remote.network.ApiErrorHandler
import com.klim.trossage_android.domain.model.Chat
import com.klim.trossage_android.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class ChatRepositoryImpl(
    private val api: ChatApiService,
    private val chatDao: ChatDao
) : ChatRepository {

    private val _chatsFlow = MutableStateFlow<List<Chat>>(emptyList())

    override fun getChatsFlow(): Flow<List<Chat>> = _chatsFlow

    override suspend fun loadChats(offset: Int, limit: Int): Result<List<Chat>> {
        return try {
            if (offset == 0) {
                val cached = chatDao.getAllChats().map { ChatMapper.fromChatEntity(it) }
                if (cached.isNotEmpty()) {
                    _chatsFlow.value = cached
                }
            }

            val response = api.getChats(limit, offset)
            if (!response.isSuccess || response.data == null) {
                return Result.failure(Exception(response.error ?: "Ошибка загрузки чатов"))
            }

            val chats = response.data.chats.map { ChatMapper.toChat(it) }

            if (offset == 0) {
                chatDao.clearAll()
                chatDao.insertAll(chats.map { ChatMapper.toChatEntity(it) })
                chatDao.deleteOldChats()
                _chatsFlow.value = chats
            } else {
                _chatsFlow.value = _chatsFlow.value + chats
            }

            Result.success(chats)
        } catch (e: Exception) {
            Result.failure(Exception(ApiErrorHandler.handleError(e)))
        }
    }

    override suspend fun createChat(companionUserId: Int): Result<Chat> {
        return try {
            val response = api.createChat(CreateChatRequest(companionUserId))
            if (!response.isSuccess || response.data == null) {
                return Result.failure(Exception(response.error ?: "Ошибка создания чата"))
            }
            val chat = ChatMapper.toChat(response.data)
            Result.success(chat)
        } catch (e: Exception) {
            Result.failure(Exception(ApiErrorHandler.handleError(e)))
        }
    }
}
