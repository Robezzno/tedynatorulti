package com.puffyai.puffyai.domain.usecase

import com.android.billingclient.api.ProductDetails
import com.puffyai.puffyai.data.BillingManager
import com.puffyai.puffyai.data.local.UserPreferences
import com.puffyai.puffyai.domain.model.PurchasePack
import javax.inject.Inject

class PurchaseCreditsUseCase @Inject constructor(
    private val billingManager: BillingManager,
    private val userPreferences: UserPreferences
) {
    fun getAvailableProducts(onResult: (List<PurchasePack>) -> Unit) {
        billingManager.queryAvailableProducts { billingResult, productDetailsList ->
            if (billingResult.responseCode == com.android.billingclient.api.BillingClient.BillingResponseCode.OK) {
                val purchasePacks = productDetailsList.map { productDetails ->
                    PurchasePack(
                        productId = productDetails.productId,
                        name = productDetails.title,
                        description = productDetails.description,
                        price = productDetails.oneTimePurchaseOfferDetails?.formattedPrice ?: "N/A",
                        credits = when (productDetails.productId) {
                            "puffy_5_generations" -> 5
                            "puffy_10_generations" -> 10
                            "puffy_20_generations" -> 20
                            else -> 0
                        }
                    )
                }
                onResult(purchasePacks)
            } else {
                onResult(emptyList()) // Or handle error more specifically
            }
        }
    }

    fun launchPurchaseFlow(productDetails: ProductDetails) {
        billingManager.launchPurchaseFlow(productDetails)
    }

    fun addCredits(productId: String) {
        when (productId) {
            "puffy_5_generations" -> userPreferences.addCredits(5)
            "puffy_10_generations" -> userPreferences.addCredits(10)
            "puffy_20_generations" -> userPreferences.addCredits(20)
        }
    }
}