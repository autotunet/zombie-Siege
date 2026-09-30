package com.example

import android.content.pm.ActivityInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ads.AdMobConstants
import com.example.ads.AdMobManager
import com.example.ui.GameScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.GameViewModel
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val tag = "MainActivity"
    private var currentRewardedAd: RewardedAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            window.setFormat(PixelFormat.RGBA_8888)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                window.colorMode = ActivityInfo.COLOR_MODE_DEFAULT
            }
        } catch (_: Throwable) {}

        enableEdgeToEdge()

        // 1. Initialize Google Mobile Ads SDK on background dispatcher to optimize startup
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Remove any corrupt zero-length variation seed files that trigger variations_seed_loader errors
                val webViewDir = java.io.File(applicationContext.dataDir, "app_webview")
                if (webViewDir.exists()) {
                    listOf("variations_seed", "variations_seed_new", "variations_stamp").forEach { name ->
                        val f = java.io.File(webViewDir, name)
                        if (f.exists() && f.length() == 0L) {
                            f.delete()
                        }
                    }
                }
                AdMobManager.getInstance(applicationContext).initSdk()
            } catch (e: Exception) {
                Log.w(tag, "MobileAds init notice: ${e.message}")
            }
        }

        setContent {
            MyApplicationTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0D1117)
                ) {
                    val gameViewModel: GameViewModel = viewModel()
                    GameScreen(viewModel = gameViewModel)
                }
            }
        }
    }

    /**
     * Helper function to load and show rewarded video ads configured in the project settings.
     *
     * @param adUnitId The AdMob Rewarded Ad Unit ID (default: Prestamevip Rewarded ca-app-pub-1495262574338316/8970040322)
     * @param onUserEarnedReward Invoked when the user completes watching the video and receives the reward
     * @param onAdDismissed Invoked when the rewarded video is dismissed or if an error occurs
     */
    fun showRewardedVideoAd(
        adUnitId: String = AdMobConstants.REWARDED_PRESTAME_VIP,
        onUserEarnedReward: () -> Unit,
        onAdDismissed: () -> Unit = {}
    ) {
        Log.d(tag, "Requesting Rewarded Video Ad ($adUnitId)...")

        val extras = Bundle().apply {
            putString("npa", "1")
        }
        val adRequest = AdRequest.Builder()
            .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
            .build()

        RewardedAd.load(
            this,
            adUnitId,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(tag, "Rewarded Video Ad loaded successfully.")
                    currentRewardedAd = ad

                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            Log.d(tag, "Rewarded Ad dismissed.")
                            currentRewardedAd = null
                            onAdDismissed()
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                            Log.e(tag, "Failed to show Rewarded Ad: ${adError.message}")
                            currentRewardedAd = null
                            onAdDismissed()
                        }
                    }

                    ad.show(this@MainActivity) { rewardItem ->
                        Log.d(tag, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                        onUserEarnedReward()
                    }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.w(tag, "Rewarded Ad failed to load ($adUnitId): ${loadAdError.message}. Using safe fallback.")
                    val isVip = (adUnitId == AdMobConstants.REWARDED_PRESTAME_VIP)
                    val handled = AdMobManager.getInstance(applicationContext).showRewardedAd(
                        activity = this@MainActivity,
                        isVip = isVip,
                        onUserEarnedReward = { _ -> onUserEarnedReward() },
                        onDismiss = onAdDismissed
                    )
                    if (!handled) {
                        onAdDismissed()
                    }
                }
            }
        )
    }

    /**
     * Helper to show the VIP Rewarded Video Ad (Prestamevip: ca-app-pub-1495262574338316/8970040322).
     */
    fun showPrestameVipRewardedAd(onUserEarnedReward: () -> Unit, onAdDismissed: () -> Unit = {}) {
        showRewardedVideoAd(
            adUnitId = AdMobConstants.REWARDED_PRESTAME_VIP,
            onUserEarnedReward = onUserEarnedReward,
            onAdDismissed = onAdDismissed
        )
    }

    /**
     * Helper to show the Secondary Rewarded Video Ad (ca-app-pub-1495262574338316/2903409205).
     */
    fun showSecondaryRewardedAd(onUserEarnedReward: () -> Unit, onAdDismissed: () -> Unit = {}) {
        showRewardedVideoAd(
            adUnitId = AdMobConstants.REWARDED_SECONDARY,
            onUserEarnedReward = onUserEarnedReward,
            onAdDismissed = onAdDismissed
        )
    }
}
