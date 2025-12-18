package com.klim.trossage_android.domain.repository

import com.klim.trossage_android.domain.model.Chat
import com.klim.trossage_android.domain.model.User
import com.klim.trossage_android.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getChatsFlow(): Flow<List<Chat>>

    suspend fun loadChats(offset: Int, limit: Int): Result<List<Chat>>

    suspend fun searchUsers(query: String): Result<List<User>>

    suspend fun createChat(companionUserId: String): Result<Chat>

    suspend fun deleteChat(chatId: String): Result<Unit>
}