package com.puffyai.puffyai.domain.repository

import android.app.Activity
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.puffyai.puffyai.domain.model.PurchasePack

interface BillingRepository {
    fun startBillingConnection()
    suspend fun queryAvailableProducts(): List<ProductDetails>
    fun launchPurchaseFlow(activity: Activity, productDetails: ProductDetails)
    fun consumePurchase(purchaseToken: String)
    fun acknowledgePurchase(purchase: Purchase, onResult: (Boolean) -> Unit)
    fun restorePurchases(onResult: (List<Purchase>) -> Unit)
    fun setPurchasesUpdatedListener(listener: (billingResult: com.android.billingclient.api.BillingResult, purchases: List<Purchase>?) -> Unit)
    fun endConnection()
}