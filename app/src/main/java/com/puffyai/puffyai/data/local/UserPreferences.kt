package com.puffyai.puffyai.data.local

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LAST_USAGE_DATE = "last_usage_date"
        private const val KEY_DAILY_USAGE_COUNT = "daily_usage_count"
        private const val KEY_PURCHASED_CREDITS = "purchased_credits"
        private const val DAILY_LIMIT = 1 // 1 free image per day
    }

    fun getRemainingGenerations(): Int {
        resetDailyUsageIfNewDay()
        val dailyUsed = prefs.getInt(KEY_DAILY_USAGE_COUNT, 0)
        val purchased = prefs.getInt(KEY_PURCHASED_CREDITS, 0)
        return (DAILY_LIMIT - dailyUsed) + purchased
    }

    fun incrementDailyUsage() {
        resetDailyUsageIfNewDay()
        val currentCount = prefs.getInt(KEY_DAILY_USAGE_COUNT, 0)
        prefs.edit().putInt(KEY_DAILY_USAGE_COUNT, currentCount + 1).apply()
    }

    fun addCredits(amount: Int) {
        val currentCredits = prefs.getInt(KEY_PURCHASED_CREDITS, 0)
        prefs.edit().putInt(KEY_PURCHASED_CREDITS, currentCredits + amount).apply()
    }

    fun consumeCredit() {
        val currentCredits = prefs.getInt(KEY_PURCHASED_CREDITS, 0)
        if (currentCredits > 0) {
            prefs.edit().putInt(KEY_PURCHASED_CREDITS, currentCredits - 1).apply()
        } else {
            incrementDailyUsage() // Consume from daily free if no purchased credits
        }
    }

    private fun resetDailyUsageIfNewDay() {
        val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val lastDate = prefs.getString(KEY_LAST_USAGE_DATE, "")

        if (today != lastDate) {
            prefs.edit()
                .putString(KEY_LAST_USAGE_DATE, today)
                .putInt(KEY_DAILY_USAGE_COUNT, 0)
                .apply()
        }
    }
}