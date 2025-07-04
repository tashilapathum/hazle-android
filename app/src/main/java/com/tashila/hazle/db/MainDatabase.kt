package com.tashila.hazle.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.tashila.hazle.db.messages.InstantConverter
import com.tashila.hazle.db.messages.MessageDao
import com.tashila.hazle.db.messages.MessageEntity
import com.tashila.hazle.db.threads.ThreadDao
import com.tashila.hazle.db.threads.ThreadEntity

@Database(entities = [MessageEntity::class, ThreadEntity::class], version = 2, exportSchema = false)
@TypeConverters(InstantConverter::class)
abstract class MainDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun threadDao(): ThreadDao
}