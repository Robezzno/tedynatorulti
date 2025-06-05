package com.puffyai.puffyai.domain.usecase

import com.puffyai.puffyai.data.local.UserPreferences
import com.puffyai.puffyai.util.DateUtils
import javax.inject.Inject

class GetUsageStatusUseCase @Inject constructor(
    private val userPreferences: UserPreferences,
    private val dateUtils: DateUtils
) {
    fun getRemainingGenerations(): Int {
        return userPreferences.getRemainingGenerations()
    }

    fun canGenerate(): Boolean {
        return userPreferences.getRemainingGenerations() > 0
    }
}