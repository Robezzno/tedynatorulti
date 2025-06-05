package com.puffyai.puffyai.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.puffyai.puffyai.domain.model.GeneratedImage

@Database(entities = [GeneratedImage::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun generatedImageDao(): GeneratedImageDao
}