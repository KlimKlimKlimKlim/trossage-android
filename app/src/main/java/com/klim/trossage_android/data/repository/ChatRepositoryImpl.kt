package com.klim.trossage_android.data.repository

import android.util.Log
import com.klim.trossage_android.data.local.room.dao.ChatDao
import com.klim.trossage_android.data.mapper.ChatMapper
import com.klim.trossage_android.data.remote.api.ChatApiService
import com.klim.trossage_android.data.remote.dto.CreateChatRequest
import com.klim.trossage_android.domain.model.Chat
import com.klim.trossage_android.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ChatRepositoryImpl(
    private val apiService: ChatApiService,
    private val chatDao: ChatDao
) : ChatRepository {

    override fun getChatsFlow(): Flow<List<Chat>> {
        return chatDao.getAllChats().map { entities ->
            entities.map { ChatMapper.fromChatEntity(it) }
        }
    }

    override suspend fun loadChats(offset: Int, limit: Int): Result<List<Chat>> {
        return try {
            val response = apiService.getChats(limit, offset)

            if (response.isSuccess && response.data != null) {
                val chats = response.data.chats.map { ChatMapper.toChat(it) }

                if (offset == 0) {
                    chatDao.deleteAll()
                }
                chatDao.insertAll(chats.map { ChatMapper.toChatEntity(it) })

                Result.success(chats)
            } else {
                Result.failure(Exception(response.error ?: "Неизвестная ошибка"))
            }
        } catch (e: Exception) {
            Log.e("ChatRepo", "loadChats error", e)
            Result.failure(e)
        }
    }

    override suspend fun createChat(companionUserId: Int): Result<Chat> {
        return try {
            Log.d("ChatRepo", "Creating chat with user $companionUserId")
            val response = apiService.createChat(CreateChatRequest(companionUserId))

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.isSuccess == true && body.data != null) {
                    Log.d("ChatRepo", "Chat created successfully")
                    val chat = ChatMapper.toChat(body.data)
                    Result.success(chat)
                } else {
                    Log.e("ChatRepo", "Create failed: ${body?.error}")
                    Result.failure(Exception(body?.error ?: "Ошибка создания чата"))
                }
            } else if (response.code() == 409) {
                Log.d("ChatRepo", "Chat exists (409), loading chats to find it")

                val chatsResponse = apiService.getChats(100, 0)
                if (chatsResponse.isSuccess && chatsResponse.data != null) {
                    val existingChat = chatsResponse.data.chats
                        .find { it.otherUser.id == companionUserId }

                    if (existingChat != null) {
                        Log.d("ChatRepo", "Found existing chat: ${existingChat.id}")
                        val chat = ChatMapper.toChat(existingChat)
                        Result.success(chat)
                    } else {
                        Log.e("ChatRepo", "Chat exists but not found in list")
                        Result.failure(Exception("Чат не найден"))
                    }
                } else {
                    Log.e("ChatRepo", "Failed to load chats")
                    Result.failure(Exception("Не удалось загрузить чаты"))
                }
            } else {
                Log.e("ChatRepo", "HTTP ${response.code()}")
                Result.failure(Exception("Ошибка ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("ChatRepo", "createChat exception", e)
            Result.failure(e)
        }
    }
}
