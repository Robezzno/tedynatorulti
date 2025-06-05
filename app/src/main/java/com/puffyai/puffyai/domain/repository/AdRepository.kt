package com.puffyai.puffyai.domain.repository

import android.app.Activity
import com.puffyai.puffyai.data.network.NetworkResponse

interface AdRepository {
    suspend fun loadRewardedAd(): NetworkResponse<Unit>
    fun showRewardedAd(activity: Activity, onReward: () -> Unit)
}