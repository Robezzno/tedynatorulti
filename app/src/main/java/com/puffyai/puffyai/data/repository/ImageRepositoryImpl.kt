package com.puffyai.puffyai.data.repository

import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import com.puffyai.puffyai.data.local.GeneratedImageDao
import com.puffyai.puffyai.data.network.NetworkResponse
import com.puffyai.puffyai.data.remote.OpenAiApiService
import com.puffyai.puffyai.domain.ImageProcessor
import com.puffyai.puffyai.domain.model.GeneratedImage
import com.puffyai.puffyai.domain.repository.ImageRepository
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.InputStream
import javax.inject.Inject

class ImageRepositoryImpl @Inject constructor(
    private val openAiApiService: OpenAiApiService,
    private val imageProcessor: ImageProcessor,
    private val generatedImageDao: GeneratedImageDao
) : ImageRepository {

    override suspend fun generateImage(imageUri: Uri, prompt: String, contentResolver: ContentResolver): NetworkResponse<String> {
        return try {
            val inputStream: InputStream? = contentResolver.openInputStream(imageUri)
            val originalBitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) {
                return NetworkResponse.Error("Failed to decode image.")
            }

            val scaledBitmap = imageProcessor.scaleBitmap(originalBitmap, 1024) // Max width 1024 for DALL-E 3
            val imageBytes = imageProcessor.compressBitmapToByteArray(scaledBitmap, Bitmap.CompressFormat.PNG, 100)

            val requestBody = imageBytes.toRequestBody("image/png".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("image", "image.png", requestBody)
            val promptPart = prompt.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = openAiApiService.generateImage(promptPart, imagePart)

            if (response.isSuccessful) {
                val imageUrl = response.body()?.data?.firstOrNull()?.url
                if (!imageUrl.isNullOrEmpty()) {
                    NetworkResponse.Success(imageUrl)
                } else {
                    NetworkResponse.Error("Image URL not found in response.")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = when (response.code()) {
                    401 -> "API Key invalid. Please check configuration."
                    429 -> "Daily limit reached or API throttling. Please wait or purchase more credits."
                    else -> "Error generating image: ${response.message()}. Details: $errorBody"
                }
                NetworkResponse.Error(errorMessage, response.code())
            }
        } catch (e: Exception) {
            NetworkResponse.Error("An error occurred: ${e.message}")
        }
    }

    override suspend fun saveGeneratedImage(image: GeneratedImage) {
        generatedImageDao.insert(image)
    }

    override fun getGeneratedImages(): Flow<List<GeneratedImage>> {
        return generatedImageDao.getAllImages()
    }
}