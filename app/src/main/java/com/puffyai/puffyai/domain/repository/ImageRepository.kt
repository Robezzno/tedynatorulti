package com.puffyai.puffyai.domain.repository

import android.graphics.Bitmap
import android.net.Uri
import com.puffyai.puffyai.data.network.NetworkResponse
import com.puffyai.puffyai.domain.model.GeneratedImage
import kotlinx.coroutines.flow.Flow

interface ImageRepository {
    suspend fun generateImage(imageUri: Uri, prompt: String, contentResolver: android.content.ContentResolver): NetworkResponse<String>
    suspend fun saveGeneratedImage(image: GeneratedImage)
    fun getGeneratedImages(): Flow<List<GeneratedImage>>
}