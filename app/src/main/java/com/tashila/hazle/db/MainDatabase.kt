package com.tashila.hazle.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [MessageEntity::class], version = 1, exportSchema = false)
@TypeConverters(InstantConverter::class)
abstract class MainDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
}