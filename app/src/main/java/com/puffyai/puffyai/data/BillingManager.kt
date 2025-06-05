package com.puffyai.puffyai.data

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.AcknowledgePurchaseResponseListener
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ConsumeResponseListener
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.puffyai.puffyai.domain.repository.BillingRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context
) : BillingRepository {

    private lateinit var billingClient: BillingClient
    private var productDetailsList = emptyList<ProductDetails>()
    private var purchasesUpdatedListener: ((billingResult: BillingResult, purchases: List<Purchase>?) -> Unit)? = null

    init {
        setupBillingClient()
    }

    private fun setupBillingClient() {
        billingClient = BillingClient.newBuilder(context)
            .setListener { billingResult, purchases ->
                purchasesUpdatedListener?.invoke(billingResult, purchases)
            }
            .enablePendingPurchases()
            .build()
    }

    override fun startBillingConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryProductDetails()
                    queryPurchases()
                } else {
                    // Handle billing setup failure
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection
                startBillingConnection()
            }
        })
    }

    override suspend fun queryAvailableProducts(): List<ProductDetails> {
        return withContext(Dispatchers.IO) {
            val productList = listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId("puffy_5_generations")
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build(),
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId("puffy_10_generations")
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build(),
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId("puffy_20_generations")
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            )

            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build()

            val (billingResult, productDetails) = billingClient.queryProductDetails(params)
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                productDetailsList = productDetails
                productDetails
            } else {
                emptyList()
            }
        }
    }

    private fun queryProductDetails() {
        CoroutineScope(Dispatchers.IO).launch {
            queryAvailableProducts()
        }
    }

    private fun queryPurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                if (purchases.isNotEmpty()) {
                    purchasesUpdatedListener?.invoke(billingResult, purchases)
                }
            } else {
                // Handle query purchases failure
            }
        }
    }

    override fun launchPurchaseFlow(activity: Activity, productDetails: ProductDetails) {
        if (!billingClient.isReady) {
            // BillingClient is not ready, handle error
            return
        }

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .build()
        )
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        billingClient.launchBillingFlow(activity, billingFlowParams)
    }

    override fun acknowledgePurchase(purchase: Purchase, onResult: (Boolean) -> Unit) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
            val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                onResult(billingResult.responseCode == BillingClient.BillingResponseCode.OK)
            }
        } else {
            onResult(false)
        }
    }

    override fun consumePurchase(purchaseToken: String) {
        val consumeParams = ConsumeParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .build()

        billingClient.consumeAsync(consumeParams, object : ConsumeResponseListener {
            override fun onConsumeResponse(billingResult: BillingResult, p1: String) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // Purchase consumed successfully
                } else {
                    // Handle consume failure
                }
            }
        })
    }

    override fun restorePurchases(onResult: (List<Purchase>) -> Unit) {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                onResult(purchases)
            } else {
                onResult(emptyList())
            }
        }
    }

    override fun setPurchasesUpdatedListener(listener: (billingResult: BillingResult, purchases: List<Purchase>?) -> Unit) {
        this.purchasesUpdatedListener = listener
    }

    override fun endConnection() {
        if (billingClient.isReady) {
            billingClient.endConnection()
        }
    }
}