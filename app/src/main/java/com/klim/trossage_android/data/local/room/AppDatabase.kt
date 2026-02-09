package com.klim.trossage_android.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.klim.trossage_android.data.local.room.dao.ChatDao
import com.klim.trossage_android.data.local.room.dao.MessageDao
import com.klim.trossage_android.data.local.room.entity.ChatEntity
import com.klim.trossage_android.data.local.room.entity.MessageEntity

@Database(
    entities = [ChatEntity::class, MessageEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun messageDao(): MessageDao
}
