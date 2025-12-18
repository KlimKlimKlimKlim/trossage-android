package com.klim.trossage_android.data.repository

import com.klim.trossage_android.data.mapper.ChatMapper
import com.klim.trossage_android.data.remote.api.ChatApiService
import com.klim.trossage_android.domain.model.Chat
import com.klim.trossage_android.domain.model.User
import com.klim.trossage_android.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class ChatRepositoryImpl(
    private val api: ChatApiService
) : ChatRepository {

    private val _chatsFlow = MutableStateFlow<List<Chat>>(emptyList())

    override fun getChatsFlow(): Flow<List<Chat>> = _chatsFlow

    override suspend fun loadChats(offset: Int, limit: Int): Result<List<Chat>> {
        return try {
            // TODO: когда появится /chat - раскомментировать
            // val response = api.getChats(offset, limit)
            // val chats = response.chats.map { ChatMapper.toChat(it) }
            // _chatsFlow.value = chats
            // Result.success(chats)
            Result.success(emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchUsers(query: String): Result<List<User>> {
        return try {
            // TODO: когда появится /users/search - раскомментировать
            // val users = api.searchUsers(query)
            // Result.success(users.map { ChatMapper.toUser(it) })
            Result.success(emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createChat(companionUserId: String): Result<Chat> {
        return try {
            // TODO: когда появится POST /chat - раскомментировать
            // val response = api.createChat(CreateChatRequest(companionUserId))
            // val chat = ChatMapper.toChat(response.chat)
            // Result.success(chat)
            Result.failure(Exception("Ручка /chat пока не реализована"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteChat(chatId: String): Result<Unit> {
        return try {
            // TODO: когда появится DELETE /chat/{id} - раскомментировать
            // api.deleteChat(chatId)
            // Result.success(Unit)
            Result.failure(Exception("Ручка DELETE /chat пока не реализована"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}