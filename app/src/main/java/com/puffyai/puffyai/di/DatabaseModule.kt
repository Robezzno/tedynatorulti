package com.puffyai.puffyai.di

import android.content.Context
import androidx.room.Room
import com.puffyai.puffyai.data.local.AppDatabase
import com.puffyai.puffyai.data.local.GeneratedImageDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "app_db")
            .fallbackToDestructiveMigration() // For development, handle migrations manually in production
            .build()

    @Provides
    @Singleton
    fun provideGeneratedImageDao(appDatabase: AppDatabase): GeneratedImageDao =
        appDatabase.generatedImageDao()
}