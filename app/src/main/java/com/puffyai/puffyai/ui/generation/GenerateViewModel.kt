package com.puffyai.puffyai.ui.generation

import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.puffyai.puffyai.data.network.NetworkResponse
import com.puffyai.puffyai.domain.usecase.GenerateImageUseCase
import com.puffyai.puffyai.domain.usecase.SaveGeneratedImageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class GenerateViewModel @Inject constructor(
    private val generateImageUseCase: GenerateImageUseCase,
    private val saveGeneratedImageUseCase: SaveGeneratedImageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState

    // Hardcoded prompt for now, can be made dynamic later
    private val imagePrompt = "Create a high-resolution 3D render of Attached image designed as an inflatable, puffy object. The shape should appear soft, rounded, and air-filled — like a plush balloon or blow-up toy. Use a smooth, matte texture with subtle fabric creases and stitching to emphasize the inflatable look.The form should be slightly irregular and squishy, with gentle shadows and soft lighting that highlight volume and realism. Place it on a clean, minimal background (light gray or pale blue), and maintain a playful, sculptural aesthetic."

    fun generateImage(imageUri: Uri, contentResolver: ContentResolver) {
        _uiState.value = UiState.Loading("Sending to AI...")
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = generateImageUseCase.execute(imageUri, contentResolver, imagePrompt)) {
                is NetworkResponse.Success -> {
                    val imageUrl = result.data
                    saveGeneratedImageUseCase.execute(imagePrompt, imageUrl) // Save to local DB
                    withContext(Dispatchers.Main) {
                        _uiState.value = UiState.Success(imageUrl)
                    }
                }
                is NetworkResponse.Error -> {
                    withContext(Dispatchers.Main) {
                        _uiState.value = UiState.Error(result.message)
                    }
                }
                NetworkResponse.Loading -> {
                    // Should not happen here as execute is a suspend function
                }
            }
        }
    }

    sealed class UiState {
        object Idle : UiState()
        data class Loading(val message: String) : UiState()
        data class Success(val imageUrl: String) : UiState()
        data class Error(val message: String) : UiState()
    }
}