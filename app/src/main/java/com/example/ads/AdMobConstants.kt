package com.example.ads

/**
 * Identificadores oficiales de Google AdMob configurados para Zombie Siege.
 */
object AdMobConstants {
    // ID de Aplicación de AdMob
    const val APP_ID = "ca-app-pub-1495262574338316~3347511713"

    // 1. Banners
    const val BANNER_STANDARD = "ca-app-pub-1495262574338316/2803163788"
    const val BANNER_VIP = "ca-app-pub-1495262574338316/2452733430" // Banner vip

    // 2. Anuncios Bonificados (Rewarded)
    const val REWARDED_PRESTAME_VIP = "ca-app-pub-1495262574338316/8970040322" // Rewarded / Prestamevip
    const val REWARDED_SECONDARY = "ca-app-pub-1495262574338316/2903409205"

    // 3. Anuncios Intersticiales (Pantalla Completa)
    const val INTERSTITIAL_WAVE_CLEAR = "ca-app-pub-1495262574338316/1182179137"
    const val INTERSTITIAL_GAME_OVER = "ca-app-pub-1495262574338316/4962596335"
    const val INTERSTITIAL_REVIVE = "ca-app-pub-1495262574338316/2714752655"
    const val INTERSTITIAL_SHOP = "ca-app-pub-1495262574338316/3503687777"
}

enum class InterstitialType {
    WAVE_CLEAR,
    GAME_OVER,
    REVIVE,
    SHOP
}
