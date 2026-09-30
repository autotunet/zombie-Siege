package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.WeaponState
import com.example.model.WeaponType
import com.example.model.ZombieType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context matches app name`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Zombie Siege", appName)
    }

    @Test
    fun `test weapon upgrade scaling formulas`() {
        val pistol = WeaponState(
            type = WeaponType.PISTOL,
            isUnlocked = true,
            upgradeLevel = 1,
            currentAmmo = WeaponType.PISTOL.magazineSize
        )

        assertEquals(WeaponType.PISTOL.baseDamage, pistol.damage, 0.01f)
        assertEquals(12, pistol.maxAmmo)

        // Upgrade to level 2
        val upgradedPistol = pistol.copy(upgradeLevel = 2)
        assertTrue(upgradedPistol.damage > pistol.damage)
        assertTrue(upgradedPistol.maxAmmo >= pistol.maxAmmo)
        assertTrue(upgradedPistol.fireRateMs <= pistol.fireRateMs)
    }

    @Test
    fun `test zombie types attributes and boss configuration`() {
        val walker = ZombieType.WALKER
        val brute = ZombieType.BRUTE
        val boss = ZombieType.BOSS

        assertTrue(brute.baseHp > walker.baseHp)
        assertTrue(boss.isBoss)
        assertTrue(boss.baseHp > brute.baseHp)
        assertTrue(boss.coinValue > walker.coinValue)
    }

    @Test
    fun `test admob constants configuration`() {
        assertEquals("ca-app-pub-1495262574338316/2452733430", com.example.ads.AdMobConstants.BANNER_VIP)
        assertEquals("ca-app-pub-1495262574338316/8970040322", com.example.ads.AdMobConstants.REWARDED_PRESTAME_VIP)
        assertEquals("ca-app-pub-1495262574338316/2803163788", com.example.ads.AdMobConstants.BANNER_STANDARD)
        assertEquals("ca-app-pub-1495262574338316/2903409205", com.example.ads.AdMobConstants.REWARDED_SECONDARY)
        assertEquals("ca-app-pub-1495262574338316/1182179137", com.example.ads.AdMobConstants.INTERSTITIAL_WAVE_CLEAR)
        assertEquals("ca-app-pub-1495262574338316/4962596335", com.example.ads.AdMobConstants.INTERSTITIAL_GAME_OVER)
        assertEquals("ca-app-pub-1495262574338316/2714752655", com.example.ads.AdMobConstants.INTERSTITIAL_REVIVE)
        assertEquals("ca-app-pub-1495262574338316/3503687777", com.example.ads.AdMobConstants.INTERSTITIAL_SHOP)
    }
}
