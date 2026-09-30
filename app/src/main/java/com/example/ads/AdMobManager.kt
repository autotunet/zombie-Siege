package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class AdMobManager private constructor(private val appContext: Context) {

    private var isInitialized = false

    private var vipRewardedAd: RewardedAd? = null
    private var isVipRewardedLoading = false

    private var secondaryRewardedAd: RewardedAd? = null
    private var isSecondaryRewardedLoading = false

    private val interstitialAds = mutableMapOf<InterstitialType, InterstitialAd?>()
    private val interstitialLoading = mutableMapOf<InterstitialType, Boolean>()

    init {
        initSdk()
    }

    private fun initSdk() {
        try {
            val reqConfig = RequestConfiguration.Builder()
                .setTestDeviceIds(listOf(AdRequest.DEVICE_ID_EMULATOR))
                .setTagForChildDirectedTreatment(RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE)
                .build()
            MobileAds.setRequestConfiguration(reqConfig)

            MobileAds.initialize(appContext) { initStatus ->
                Log.d(TAG, "AdMob SDK Initialized: $initStatus")
                isInitialized = true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize AdMob SDK", e)
        }
    }

    fun preloadAds() {
        // Ads are loaded on-demand when requested by the user to avoid premature headless video playback
    }

    private fun buildAdRequest(): AdRequest {
        val extras = android.os.Bundle().apply {
            putString("npa", "1")
        }
        return AdRequest.Builder()
            .addNetworkExtrasBundle(com.google.ads.mediation.admob.AdMobAdapter::class.java, extras)
            .build()
    }

    private fun preloadVipRewarded() {
        if (vipRewardedAd != null || isVipRewardedLoading) return
        isVipRewardedLoading = true
        Log.d(TAG, "Loading VIP Rewarded Ad (${AdMobConstants.REWARDED_PRESTAME_VIP})...")

        RewardedAd.load(
            appContext,
            AdMobConstants.REWARDED_PRESTAME_VIP,
            buildAdRequest(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "VIP Rewarded Ad loaded successfully")
                    vipRewardedAd = ad
                    isVipRewardedLoading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "VIP Rewarded Ad failed to load: ${error.message} (code: ${error.code})")
                    vipRewardedAd = null
                    isVipRewardedLoading = false
                }
            }
        )
    }

    private fun preloadSecondaryRewarded() {
        if (secondaryRewardedAd != null || isSecondaryRewardedLoading) return
        isSecondaryRewardedLoading = true
        Log.d(TAG, "Loading Secondary Rewarded Ad (${AdMobConstants.REWARDED_SECONDARY})...")

        RewardedAd.load(
            appContext,
            AdMobConstants.REWARDED_SECONDARY,
            buildAdRequest(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "Secondary Rewarded Ad loaded successfully")
                    secondaryRewardedAd = ad
                    isSecondaryRewardedLoading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "Secondary Rewarded Ad failed to load: ${error.message} (code: ${error.code})")
                    secondaryRewardedAd = null
                    isSecondaryRewardedLoading = false
                }
            }
        )
    }

    fun preloadInterstitial(type: InterstitialType) {
        if (interstitialAds[type] != null || interstitialLoading[type] == true) return
        interstitialLoading[type] = true

        val unitId = when (type) {
            InterstitialType.WAVE_CLEAR -> AdMobConstants.INTERSTITIAL_WAVE_CLEAR
            InterstitialType.GAME_OVER -> AdMobConstants.INTERSTITIAL_GAME_OVER
            InterstitialType.REVIVE -> AdMobConstants.INTERSTITIAL_REVIVE
            InterstitialType.SHOP -> AdMobConstants.INTERSTITIAL_SHOP
        }

        Log.d(TAG, "Loading Interstitial Ad for $type ($unitId)...")

        InterstitialAd.load(
            appContext,
            unitId,
            buildAdRequest(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Interstitial Ad for $type loaded successfully")
                    interstitialAds[type] = ad
                    interstitialLoading[type] = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "Interstitial Ad for $type failed: ${error.message} (code: ${error.code})")
                    interstitialAds[type] = null
                    interstitialLoading[type] = false
                }
            }
        )
    }

    /**
     * Shows a Rewarded Video Ad.
     * If the real Ad is available, displays it.
     * Returns true if a real Ad was shown, or false if not ready (invoker can fall back to simulation dialog).
     */
    fun showRewardedAd(
        activity: Activity,
        isVip: Boolean = true,
        onUserEarnedReward: (RewardItem) -> Unit,
        onDismiss: () -> Unit
    ): Boolean {
        val targetAd = if (isVip) vipRewardedAd else secondaryRewardedAd

        if (targetAd == null) {
            // Not ready yet, start preload for next time
            if (isVip) preloadVipRewarded() else preloadSecondaryRewarded()
            return false
        }

        targetAd.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Rewarded Ad dismissed")
                if (isVip) {
                    vipRewardedAd = null
                    preloadVipRewarded()
                } else {
                    secondaryRewardedAd = null
                    preloadSecondaryRewarded()
                }
                onDismiss()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(TAG, "Rewarded Ad failed to show: ${adError.message}")
                if (isVip) {
                    vipRewardedAd = null
                    preloadVipRewarded()
                } else {
                    secondaryRewardedAd = null
                    preloadSecondaryRewarded()
                }
                onDismiss()
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Rewarded Ad showed fullscreen content")
            }
        }

        targetAd.show(activity) { rewardItem ->
            Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
            onUserEarnedReward(rewardItem)
        }

        return true
    }

    /**
     * Shows an Interstitial Ad if available.
     * Returns true if shown, false if not ready.
     */
    fun showInterstitialAd(
        activity: Activity,
        type: InterstitialType,
        onDismiss: () -> Unit = {}
    ): Boolean {
        val ad = interstitialAds[type]
        if (ad == null) {
            preloadInterstitial(type)
            return false
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Interstitial $type dismissed")
                interstitialAds[type] = null
                preloadInterstitial(type)
                onDismiss()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(TAG, "Interstitial $type failed to show: ${adError.message}")
                interstitialAds[type] = null
                preloadInterstitial(type)
                onDismiss()
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Interstitial $type showed fullscreen content")
            }
        }

        ad.show(activity)
        return true
    }

    companion object {
        private const val TAG = "AdMobManager"

        @Volatile
        private var INSTANCE: AdMobManager? = null

        fun getInstance(context: Context): AdMobManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AdMobManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
