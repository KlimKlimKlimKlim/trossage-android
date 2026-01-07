package com.klim.trossage_android.domain.repository

import com.klim.trossage_android.domain.model.Chat
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getChatsFlow(): Flow<List<Chat>>
    suspend fun loadChats(offset: Int, limit: Int): Result<List<Chat>>
    suspend fun createChat(companionUserId: Int): Result<Chat>
}
