package com.puffyai.puffyai.ui.purchase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.puffyai.puffyai.data.BillingManager
import com.puffyai.puffyai.data.local.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PurchaseViewModel @Inject constructor(
    private val billingManager: BillingManager,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState

    private val _availableProducts = MutableStateFlow<List<ProductDetails>>(emptyList())
    val availableProducts: StateFlow<List<ProductDetails>> = _availableProducts

    private val _currentCredits = MutableStateFlow(0)
    val currentCredits: StateFlow<Int> = _currentCredits

    init {
        billingManager.setPurchasesUpdatedListener { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                for (purchase in purchases) {
                    handlePurchase(purchase)
                }
            } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                _uiState.value = UiState.Error("Purchase cancelled.")
            } else {
                _uiState.value = UiState.Error("Error: ${billingResult.debugMessage}")
            }
        }
        billingManager.startConnection()
        updateCurrentCredits()
    }

    fun queryAvailableProducts() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading("Loading products...")
            billingManager.queryAvailableProducts { billingResult, productDetails ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    _availableProducts.value = productDetails
                    _uiState.value = UiState.Idle
                } else {
                    _uiState.value = UiState.Error("Failed to load products: ${billingResult.debugMessage}")
                }
            }
        }
    }

    fun purchaseProduct(productId: String) {
        val productDetails = _availableProducts.value.find { it.productId == productId }
        if (productDetails != null) {
            _uiState.value = UiState.Loading("Initiating purchase...")
            billingManager.launchPurchaseFlow(productDetails)
        } else {
            _uiState.value = UiState.Error("Product not found: $productId")
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                billingManager.acknowledgePurchase(purchase) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        grantCredits(purchase.products[0])
                        billingManager.consumePurchase(purchase)
                        _uiState.value = UiState.Success("Purchase successful! Credits added.")
                    } else {
                        _uiState.value = UiState.Error("Failed to acknowledge purchase: ${billingResult.debugMessage}")
                    }
                }
            } else {
                // Already acknowledged, just consume if it's a consumable
                billingManager.consumePurchase(purchase)
                grantCredits(purchase.products[0])
                _uiState.value = UiState.Success("Purchase already acknowledged. Credits granted.")
            }
        }
    }

    private fun grantCredits(productId: String) {
        when (productId) {
            "puffy_5_generations" -> userPreferences.addCredits(5)
            "puffy_10_generations" -> userPreferences.addCredits(10)
            "puffy_20_generations" -> userPreferences.addCredits(20)
        }
        updateCurrentCredits()
    }

    fun updateCurrentCredits() {
        _currentCredits.value = userPreferences.getRemainingGenerations()
    }

    override fun onCleared() {
        super.onCleared()
        billingManager.endConnection()
    }

    sealed class UiState {
        object Idle : UiState()
        data class Loading(val message: String) : UiState()
        data class Success(val message: String) : UiState()
        data class Error(val message: String) : UiState()
    }
}