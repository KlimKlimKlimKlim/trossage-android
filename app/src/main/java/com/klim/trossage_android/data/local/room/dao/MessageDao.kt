package com.klim.trossage_android.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.klim.trossage_android.data.local.room.entity.MessageEntity

@Dao
interface MessageDao {

    @Query("SELECT * FROM messages WHERE chat_id = :chatId ORDER BY timestamp DESC LIMIT 100")
    suspend fun getMessagesByChatId(chatId: Int): List<MessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<MessageEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Query("DELETE FROM messages WHERE chat_id = :chatId AND id NOT IN (SELECT id FROM messages WHERE chat_id = :chatId ORDER BY timestamp DESC LIMIT 100)")
    suspend fun deleteOldMessages(chatId: Int)

    @Query("DELETE FROM messages WHERE chat_id = :chatId")
    suspend fun clearChatMessages(chatId: Int)
}
