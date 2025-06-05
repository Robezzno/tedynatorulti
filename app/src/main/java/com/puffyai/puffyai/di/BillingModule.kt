package com.puffyai.puffyai.di

import android.content.Context
import com.android.billingclient.api.PurchasesUpdatedListener
import com.puffyai.puffyai.data.BillingManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BillingModule {

    @Provides
    @Singleton
    fun providePurchasesUpdatedListener(): PurchasesUpdatedListener {
        // This listener will be provided to BillingManager.
        // You might want to implement a more sophisticated listener that
        // communicates with your ViewModel or a central repository.
        return PurchasesUpdatedListener { billingResult, purchases ->
            // Handle purchases here or delegate to a repository/use case
            // For now, it's handled within PurchaseViewModel
        }
    }

    @Provides
    @Singleton
    fun provideBillingManager(
        @ApplicationContext context: Context,
        purchasesUpdatedListener: PurchasesUpdatedListener
    ): BillingManager {
        return BillingManager(context, purchasesUpdatedListener)
    }
}