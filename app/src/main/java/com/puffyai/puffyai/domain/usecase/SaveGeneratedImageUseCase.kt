package com.puffyai.puffyai.domain.usecase

import com.puffyai.puffyai.domain.model.GeneratedImage
import com.puffyai.puffyai.domain.repository.ImageRepository
import javax.inject.Inject

class SaveGeneratedImageUseCase @Inject constructor(
    private val imageRepository: ImageRepository
) {
    suspend fun execute(prompt: String, imageUrl: String) {
        val generatedImage = GeneratedImage(
            prompt = prompt,
            imageUrl = imageUrl,
            timestamp = System.currentTimeMillis()
        )
        imageRepository.saveGeneratedImage(generatedImage)
    }
}