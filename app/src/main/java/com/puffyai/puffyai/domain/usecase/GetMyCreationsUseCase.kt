package com.puffyai.puffyai.domain.usecase

import com.puffyai.puffyai.domain.model.GeneratedImage
import com.puffyai.puffyai.domain.repository.ImageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMyCreationsUseCase @Inject constructor(
    private val imageRepository: ImageRepository
) {
    fun execute(): Flow<List<GeneratedImage>> {
        return imageRepository.getGeneratedImages()
    }
}