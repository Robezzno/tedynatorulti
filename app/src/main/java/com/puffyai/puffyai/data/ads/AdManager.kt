package com.puffyai.puffyai.data.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.puffyai.puffyai.data.network.NetworkResponse
import com.puffyai.puffyai.domain.repository.AdRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class AdRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AdRepository {

    private var rewardedAd: RewardedAd? = null
    private val AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917" // Test Ad Unit ID

    override suspend fun loadRewardedAd(): NetworkResponse<Unit> =
        suspendCancellableCoroutine { continuation ->
            val adRequest = AdRequest.Builder().build()
            RewardedAd.load(context, AD_UNIT_ID, adRequest, object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    Log.d("AdRepositoryImpl", "Ad was loaded.")
                    if (continuation.isActive) {
                        continuation.resume(NetworkResponse.Success(Unit))
                    }
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d("AdRepositoryImpl", "Ad failed to load: ${adError.message}")
                    rewardedAd = null
                    if (continuation.isActive) {
                        continuation.resume(NetworkResponse.Error("Failed to load ad: ${adError.message}", adError.code))
                    }
                }
            })
        }

    override fun showRewardedAd(activity: Activity, onReward: () -> Unit) {
        if (rewardedAd != null) {
            rewardedAd?.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                override fun onAdClicked() {
                    Log.d("AdRepositoryImpl", "Ad was clicked.")
                }

                override fun onAdDismissedFullScreenContent() {
                    Log.d("AdRepositoryImpl", "Ad dismissed fullscreen content.")
                    rewardedAd = null
                    // Load a new ad after the current one is dismissed
                    // This should ideally be handled by a ViewModel observing the ad loading status
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e("AdRepositoryImpl", "Ad failed to show fullscreen content: ${adError.message}")
                    rewardedAd = null
                }

                override fun onAdImpression() {
                    Log.d("AdRepositoryImpl", "Ad recorded an impression.")
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d("AdRepositoryImpl", "Ad showed fullscreen content.")
                }
            }

            rewardedAd?.show(activity) { rewardItem ->
                val rewardAmount = rewardItem.amount
                val rewardType = rewardItem.type
                Log.d("AdRepositoryImpl", "User earned the reward. Amount: $rewardAmount, Type: $rewardType")
                if (rewardAmount > 0) {
                    onReward()
                } else {
                    Log.w("AdRepositoryImpl", "Reward amount is 0 or less. Not granting reward.")
                }
            }
        } else {
            Log.d("AdRepositoryImpl", "The rewarded ad wasn't ready yet.")
            // Optionally, inform the user that the ad is not ready
        }
    }
}