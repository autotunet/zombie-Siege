package com.example.model

import androidx.compose.ui.graphics.Color

enum class ZombieType(
    val displayName: String,
    val baseHp: Float,
    val baseSpeed: Float,
    val baseDamage: Float,
    val pointValue: Int,
    val coinValue: Int,
    val radius: Float,
    val skinColor: Color,
    val eyeColor: Color,
    val clothesColor: Color,
    val isBoss: Boolean = false
) {
    WALKER(
        displayName = "Caminante",
        baseHp = 45f,
        baseSpeed = 0.045f,
        baseDamage = 8f,
        pointValue = 50,
        coinValue = 5,
        radius = 24f,
        skinColor = Color(0xFF558B2F),
        eyeColor = Color(0xFFFF1744),
        clothesColor = Color(0xFF37474F)
    ),
    RUNNER(
        displayName = "Velocista",
        baseHp = 30f,
        baseSpeed = 0.085f,
        baseDamage = 12f,
        pointValue = 75,
        coinValue = 8,
        radius = 20f,
        skinColor = Color(0xFF7CB342),
        eyeColor = Color(0xFFFFEA00),
        clothesColor = Color(0xFFBF360C)
    ),
    BRUTE(
        displayName = "Tanque Blindado",
        baseHp = 180f,
        baseSpeed = 0.025f,
        baseDamage = 25f,
        pointValue = 160,
        coinValue = 20,
        radius = 34f,
        skinColor = Color(0xFF33691E),
        eyeColor = Color(0xFFFF1744),
        clothesColor = Color(0xFF263238)
    ),
    TOXIC(
        displayName = "Zombi Tóxico",
        baseHp = 60f,
        baseSpeed = 0.05f,
        baseDamage = 15f,
        pointValue = 100,
        coinValue = 12,
        radius = 26f,
        skinColor = Color(0xFF00E676),
        eyeColor = Color(0xFF76FF03),
        clothesColor = Color(0xFF1B5E20)
    ),
    BOSS(
        displayName = "Mutante Gigante",
        baseHp = 650f,
        baseSpeed = 0.022f,
        baseDamage = 45f,
        pointValue = 500,
        coinValue = 100,
        radius = 48f,
        skinColor = Color(0xFFD50000),
        eyeColor = Color(0xFF00E5FF),
        clothesColor = Color(0xFF212121),
        isBoss = true
    )
}

data class Zombie(
    val id: Long,
    val type: ZombieType,
    var x: Float, // Normalized 0f to 1f (relative to arena width)
    var y: Float, // 0f (top) to 1f (barricade line at bottom)
    val maxHp: Float,
    var currentHp: Float,
    val speed: Float,
    val damage: Float,
    val pointValue: Int,
    val coinValue: Int,
    val radius: Float,
    var walkAnim: Float = 0f,
    var hitFlashTimer: Float = 0f, // 0f to 1f
    var attackCooldown: Float = 0f,
    var isFrozen: Boolean = false,
    var freezeTimer: Float = 0f
)

enum class WeaponType(
    val id: String,
    val displayName: String,
    val description: String,
    val baseDamage: Float,
    val fireRateMs: Long,
    val magazineSize: Int,
    val reloadTimeMs: Long,
    val basePrice: Int,
    val unlockWave: Int,
    val pellets: Int = 1,
    val spreadRad: Float = 0f,
    val isAutomatic: Boolean = false,
    val isExplosive: Boolean = false,
    val explosionRadius: Float = 0f,
    val piercingCount: Int = 1,
    val bulletColor: Color = Color(0xFFFFD54F),
    val isModernAdvanced: Boolean = false
) {
    PISTOL(
        id = "pistol",
        displayName = "Pistola Táctica",
        description = "Arma secundaria confiable con munición infinita y recarga veloz.",
        baseDamage = 25f,
        fireRateMs = 280L,
        magazineSize = 12,
        reloadTimeMs = 1100L,
        basePrice = 0,
        unlockWave = 1,
        pellets = 1,
        bulletColor = Color(0xFFFFEE58),
        isModernAdvanced = false
    ),
    SHOTGUN(
        id = "shotgun",
        displayName = "Escopeta Cal. 12",
        description = "Dispara 6 postas letales con enorme poder de dispersión y frenado.",
        baseDamage = 18f, // x 6 pellets = 108 total potential damage
        fireRateMs = 650L,
        magazineSize = 8,
        reloadTimeMs = 1800L,
        basePrice = 120,
        unlockWave = 2,
        pellets = 6,
        spreadRad = 0.18f,
        bulletColor = Color(0xFFFF7043),
        isModernAdvanced = false
    ),
    ASSAULT_RIFLE(
        id = "assault_rifle",
        displayName = "Fusil de Asalto M4",
        description = "Disparo automático de alta cadencia para aniquilar hordas veloces.",
        baseDamage = 32f,
        fireRateMs = 120L,
        magazineSize = 30,
        reloadTimeMs = 1500L,
        basePrice = 280,
        unlockWave = 3,
        pellets = 1,
        isAutomatic = true,
        bulletColor = Color(0xFF64B5F6),
        isModernAdvanced = false
    ),
    SNIPER(
        id = "sniper",
        displayName = "Francotirador Pesado",
        description = "Calibre .50 antimaterial que perfora hasta 4 zombis en una sola línea.",
        baseDamage = 140f,
        fireRateMs = 950L,
        magazineSize = 5,
        reloadTimeMs = 2000L,
        basePrice = 450,
        unlockWave = 4,
        pellets = 1,
        piercingCount = 4,
        bulletColor = Color(0xFF00E676),
        isModernAdvanced = true
    ),
    GRENADE_LAUNCHER(
        id = "rpg",
        displayName = "Lanzacohetes RPG",
        description = "Proyectiles explosivos pesados que detonan causando aniquilación en área.",
        baseDamage = 220f,
        fireRateMs = 1300L,
        magazineSize = 4,
        reloadTimeMs = 2400L,
        basePrice = 750,
        unlockWave = 6,
        pellets = 1,
        isExplosive = true,
        explosionRadius = 120f,
        bulletColor = Color(0xFFFF1744),
        isModernAdvanced = true
    ),
    PLASMA_CANNON(
        id = "plasma",
        displayName = "Cañón de Plasma",
        description = "Tecnología experimental que desintegra la carne zombi con energía iónica.",
        baseDamage = 75f,
        fireRateMs = 180L,
        magazineSize = 40,
        reloadTimeMs = 1600L,
        basePrice = 1200,
        unlockWave = 8,
        pellets = 1,
        isAutomatic = true,
        bulletColor = Color(0xFF00E5FF),
        isModernAdvanced = true
    )
}

data class WeaponState(
    val type: WeaponType,
    var isUnlocked: Boolean,
    var upgradeLevel: Int = 1, // 1 to 5
    var currentAmmo: Int,
    var isReloading: Boolean = false,
    var reloadProgress: Float = 0f, // 0f to 1f
    var unlockedViaAd: Boolean = false
) {
    val damage: Float
        get() = type.baseDamage * (1f + (upgradeLevel - 1) * 0.25f)

    val maxAmmo: Int
        get() = (type.magazineSize * (1f + (upgradeLevel - 1) * 0.20f)).toInt()

    val fireRateMs: Long
        get() = (type.fireRateMs * (1f - (upgradeLevel - 1) * 0.08f)).toLong().coerceAtLeast(60L)

    val reloadTimeMs: Long
        get() = (type.reloadTimeMs * (1f - (upgradeLevel - 1) * 0.10f)).toLong().coerceAtLeast(600L)

    val nextUpgradeCost: Int
        get() = (type.basePrice * 0.6f * upgradeLevel + 60 * upgradeLevel).toInt()
}

data class Projectile(
    val id: Long,
    var x: Float, // Normalized 0f to 1f
    var y: Float, // Normalized 0f to 1f
    val vx: Float,
    val vy: Float,
    val damage: Float,
    val weaponType: WeaponType,
    var remainingPierce: Int = 1,
    val isExplosive: Boolean = false,
    val explosionRadius: Float = 0f,
    val color: Color = Color(0xFFFFD54F),
    var active: Boolean = true
)

data class Particle(
    val id: Long,
    var x: Float,
    var y: Float,
    val vx: Float,
    val vy: Float,
    val size: Float,
    val color: Color,
    var alpha: Float = 1f,
    var life: Float = 1f, // 1.0 down to 0.0
    val decayRate: Float = 0.035f,
    val text: String? = null
)

data class GroundDecal(
    val id: Long,
    val x: Float,
    val y: Float,
    val radius: Float,
    val color: Color,
    var alpha: Float = 0.85f,
    val isScorch: Boolean = false
)

data class ShellCasing(
    val id: Long,
    var x: Float,
    var y: Float,
    val vx: Float,
    val vy: Float,
    var rotation: Float = 0f,
    var alpha: Float = 1f
)

enum class SupportItem(
    val id: String,
    val displayName: String,
    val description: String,
    val cost: Int,
    val iconEmoji: String
) {
    GRENADE(
        id = "grenade",
        displayName = "Granada Frag",
        description = "Detonación instantánea en el centro que causa 180 de daño masivo a todos los zombis.",
        cost = 40,
        iconEmoji = "💣"
    ),
    CRYO_BOMB(
        id = "cryo",
        displayName = "Criobomba",
        description = "Congela y paraliza a todos los zombis en pantalla durante 4 segundos.",
        cost = 55,
        iconEmoji = "❄️"
    ),
    AIRSTRIKE(
        id = "airstrike",
        displayName = "Ataque Aéreo",
        description = "Lluvia de misiles que aniquila o hiere masivamente a la horda entera.",
        cost = 110,
        iconEmoji = "🚀"
    ),
    REPAIR_KIT(
        id = "repair",
        displayName = "Reparar Muro",
        description = "Restaura de inmediato el 40% de la salud de tu barricada.",
        cost = 35,
        iconEmoji = "🛠️"
    )
}

enum class GameStatus {
    MENU,
    PLAYING,
    WAVE_INTERMISSION,
    PAUSED,
    GAME_OVER,
    VICTORY
}

data class WaveConfig(
    val waveNumber: Int,
    val totalZombies: Int,
    val spawnIntervalMs: Long,
    val speedMultiplier: Float,
    val healthMultiplier: Float,
    val title: String,
    val subtitle: String,
    val hasBoss: Boolean = false
)
