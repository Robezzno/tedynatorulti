package com.puffyai.puffyai.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.puffyai.puffyai.domain.usecase.CheckUsageLimitUseCase
import com.puffyai.puffyai.domain.usecase.IncrementUsageUseCase
import com.puffyai.puffyai.data.AdManager
import com.puffyai.puffyai.data.BillingManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val checkUsageLimitUseCase: CheckUsageLimitUseCase,
    private val incrementUsageUseCase: IncrementUsageUseCase,
    private val adManager: AdManager,
    private val billingManager: BillingManager
) : ViewModel() {

    private val _usageStatus = MutableStateFlow(0)
    val usageStatus: StateFlow<Int> = _usageStatus

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState

    init {
        updateUsageStatus()
    }

    fun updateUsageStatus() {
        viewModelScope.launch {
            _usageStatus.value = checkUsageLimitUseCase.getRemainingGenerations()
        }
    }

    fun canGenerateImage(): Boolean {
        return checkUsageLimitUseCase.canGenerate()
    }

    fun watchAdForCredit(onReward: () -> Unit) {
        _uiState.value = UiState.Loading
        adManager.showAd {
            incrementUsageUseCase.incrementUsage()
            updateUsageStatus()
            onReward()
            _uiState.value = UiState.Idle
        }
    }

    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        data class Error(val message: String) : UiState()
        object Success : UiState()
    }
}