package com.puffyai.puffyai.di

import android.content.ContentResolver
import android.content.Context
import com.puffyai.puffyai.data.ads.AdRepositoryImpl
import com.puffyai.puffyai.data.billing.BillingRepositoryImpl
import com.puffyai.puffyai.data.local.GeneratedImageDao
import com.puffyai.puffyai.data.local.UserPreferences
import com.puffyai.puffyai.data.remote.OpenAiApiService
import com.puffyai.puffyai.data.repository.ImageRepositoryImpl
import com.puffyai.puffyai.domain.ImageProcessor
import com.puffyai.puffyai.domain.repository.AdRepository
import com.puffyai.puffyai.domain.repository.BillingRepository
import com.puffyai.puffyai.domain.repository.ImageRepository
import com.puffyai.puffyai.domain.usecase.CheckUsageLimitUseCase
import com.puffyai.puffyai.domain.usecase.GenerateImageUseCase
import com.puffyai.puffyai.domain.usecase.GetUsageStatusUseCase
import com.puffyai.puffyai.domain.usecase.IncrementUsageUseCase
import com.puffyai.puffyai.domain.usecase.PurchaseCreditsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideUserPreferences(@ApplicationContext context: Context): UserPreferences {
        return UserPreferences(context)
    }

    @Provides
    @Singleton
    fun provideCheckUsageLimitUseCase(userPreferences: UserPreferences): CheckUsageLimitUseCase {
        return CheckUsageLimitUseCase(userPreferences)
    }

    @Provides
    @Singleton
    fun provideIncrementUsageUseCase(userPreferences: UserPreferences): IncrementUsageUseCase {
        return IncrementUsageUseCase(userPreferences)
    }

    @Provides
    @Singleton
    fun provideImageProcessor(): ImageProcessor {
        return ImageProcessor()
    }

    @Provides
    @Singleton
    fun provideImageRepository(
        openAiApiService: OpenAiApiService,
        imageProcessor: ImageProcessor,
        generatedImageDao: GeneratedImageDao
    ): ImageRepository {
        return ImageRepositoryImpl(openAiApiService, imageProcessor, generatedImageDao)
    }

    @Provides
    @Singleton
    fun provideGenerateImageUseCase(
        openAiApiService: OpenAiApiService,
        imageProcessor: ImageProcessor,
        userPreferences: UserPreferences
    ): GenerateImageUseCase {
        return GenerateImageUseCase(openAiApiService, imageProcessor, userPreferences)
    }

    @Provides
    @Singleton
    fun provideGetUsageStatusUseCase(userPreferences: UserPreferences): GetUsageStatusUseCase {
        return GetUsageStatusUseCase(userPreferences)
    }

    @Provides
    @Singleton
    fun provideAdRepository(@ApplicationContext context: Context): AdRepository {
        return AdRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideBillingRepository(@ApplicationContext context: Context): BillingRepository {
        return BillingRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun providePurchaseCreditsUseCase(
        billingRepository: BillingRepository,
        userPreferences: UserPreferences
    ): PurchaseCreditsUseCase {
        return PurchaseCreditsUseCase(billingRepository, userPreferences)
    }

    @Provides
    fun provideContentResolver(@ApplicationContext context: Context): ContentResolver {
        return context.contentResolver
    }
}