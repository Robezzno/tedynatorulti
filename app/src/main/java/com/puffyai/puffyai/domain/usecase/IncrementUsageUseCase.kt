package com.puffyai.puffyai.domain.usecase

import com.puffyai.puffyai.data.local.UserPreferences

class IncrementUsageUseCase(private val userPreferences: UserPreferences) {
    fun incrementUsage() {
        userPreferences.consumeCredit()
    }
}