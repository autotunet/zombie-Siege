package com.example.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ads.AdMobConstants
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

@Composable
fun AdMobBanner(
    adUnitId: String = AdMobConstants.BANNER_STANDARD,
    isVip: Boolean = false,
    modifier: Modifier = Modifier
) {
    var adLoaded by remember { mutableStateOf(false) }
    var adError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val borderColor = if (isVip) Color(0xFFFFD54F) else Color(0xFF4285F4)
    val tagColor = if (isVip) Color(0xFFFFB300) else Color(0xFF1E88E5)
    val titleText = if (isVip) "ADMOB BANNER VIP" else "GOOGLE ADMOB BANNER"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF161B22))
            .border(1.dp, borderColor.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header tag
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 2.dp)
            ) {
                if (isVip) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFD54F),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(tagColor)
                        .padding(horizontal = 6.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = titleText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = adUnitId.takeLast(10),
                    fontSize = 9.sp,
                    color = Color(0xFF8B949E)
                )
            }

            // Real AndroidView hosting AdView
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    modifier = Modifier.testTag(if (isVip) "admob_banner_vip" else "admob_banner_standard"),
                    factory = { ctx ->
                        AdView(ctx).apply {
                            setLayerType(android.view.View.LAYER_TYPE_SOFTWARE, null)
                            setAdSize(AdSize.BANNER)
                            this.adUnitId = adUnitId
                            adListener = object : AdListener() {
                                override fun onAdLoaded() {
                                    super.onAdLoaded()
                                    adLoaded = true
                                    adError = null
                                    Log.d("AdMobBanner", "Banner loaded for $adUnitId")
                                }

                                override fun onAdFailedToLoad(error: LoadAdError) {
                                    super.onAdFailedToLoad(error)
                                    adLoaded = false
                                    adError = error.message
                                    Log.w("AdMobBanner", "Banner failed for $adUnitId: ${error.message} (code: ${error.code})")
                                }
                            }
                            val extras = android.os.Bundle().apply { putString("npa", "1") }
                            loadAd(
                                AdRequest.Builder()
                                    .addNetworkExtrasBundle(com.google.ads.mediation.admob.AdMobAdapter::class.java, extras)
                                    .build()
                            )
                        }
                    }
                )

                // Placeholder preview while loading or if fill pending in emulator
                if (!adLoaded) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = if (isVip) "🌟 ANUNCIO VIP PATROCINADO" else "📢 ANUNCIO PATROCINADO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isVip) Color(0xFFFFD54F) else Color(0xFF90CAF9)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (adError != null) "• En espera de impresión" else "• Conectando AdMob...",
                            fontSize = 10.sp,
                            color = Color(0xFF8B949E)
                        )
                    }
                }
            }
        }
    }
}
