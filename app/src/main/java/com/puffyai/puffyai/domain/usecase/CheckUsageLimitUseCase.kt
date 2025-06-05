package com.puffyai.puffyai.domain.usecase

import com.puffyai.puffyai.data.local.UserPreferences

class CheckUsageLimitUseCase(private val userPreferences: UserPreferences) {
    fun canGenerate(): Boolean {
        return userPreferences.getRemainingGenerations() > 0
    }
}