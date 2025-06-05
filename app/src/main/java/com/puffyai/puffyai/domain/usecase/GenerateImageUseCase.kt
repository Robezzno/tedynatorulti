package com.puffyai.puffyai.domain.usecase

import android.graphics.Bitmap
import android.net.Uri
import com.puffyai.puffyai.data.remote.OpenAiApiService
import com.puffyai.puffyai.data.network.NetworkResponse
import com.puffyai.puffyai.domain.ImageProcessor
import com.puffyai.puffyai.data.local.UserPreferences
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.InputStream
import javax.inject.Inject

class GenerateImageUseCase @Inject constructor(
    private val openAiApiService: OpenAiApiService,
    private val imageProcessor: ImageProcessor,
    private val userPreferences: UserPreferences
) {
    suspend fun execute(imageUri: Uri, contentResolver: android.content.ContentResolver, prompt: String): NetworkResponse<String> {
        return try {
            val inputStream: InputStream? = contentResolver.openInputStream(imageUri)
            val originalBitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) {
                return NetworkResponse.Error("Failed to decode image.")
            }

            val scaledBitmap = imageProcessor.scaleBitmap(originalBitmap, 1024)
            val imageBytes = imageProcessor.compressBitmapToByteArray(scaledBitmap, Bitmap.CompressFormat.PNG, 100)

            val requestBody = imageBytes.toRequestBody("image/png".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("image", "image.png", requestBody)
            val promptPart = prompt.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = openAiApiService.generateImage(promptPart, imagePart)

            if (response.isSuccessful) {
                val imageUrl = response.body()?.data?.firstOrNull()?.url
                if (!imageUrl.isNullOrEmpty()) {
                    userPreferences.consumeCredit()
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
}