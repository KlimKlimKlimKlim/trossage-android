package com.klim.trossage_android.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.klim.trossage_android.data.local.room.entity.ChatEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    @Query("SELECT * FROM chats ORDER BY last_message_timestamp DESC LIMIT 50")
    fun getAllChats(): Flow<List<ChatEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(chats: List<ChatEntity>)

    @Query("DELETE FROM chats WHERE id NOT IN (SELECT id FROM chats ORDER BY last_message_timestamp DESC LIMIT 50)")
    suspend fun deleteOldChats()

    @Query("DELETE FROM chats")
    suspend fun deleteAll()

    @Query("DELETE FROM chats")
    suspend fun clearAll()
}
