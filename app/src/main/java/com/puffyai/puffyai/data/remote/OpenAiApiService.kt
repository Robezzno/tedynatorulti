package com.puffyai.puffyai.data.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST

interface OpenAiApiService {
    @Multipart
    @POST("v1/images/generations") // DALL-E 3 endpoint for image generation
    suspend fun generateImage(
        @Part("prompt") prompt: RequestBody,
        @Part image: MultipartBody.Part // For image input, if DALL-E 3 supports it directly
    ): retrofit2.Response<OpenAiImageResponse> // Assuming OpenAiImageResponse will parse the JSON
}