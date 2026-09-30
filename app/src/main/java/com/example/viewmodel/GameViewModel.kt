package com.example.viewmodel

import android.app.Application
import android.app.Activity
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ads.AdMobManager
import com.example.ads.InterstitialType
import com.example.audio.GameAudio
import com.example.data.GameDatabase
import com.example.data.GameRecordEntity
import com.example.data.GameRepository
import com.example.data.PlayerProfileEntity
import com.example.model.GameStatus
import com.example.model.GroundDecal
import com.example.model.Particle
import com.example.model.Projectile
import com.example.model.ShellCasing
import com.example.model.SupportItem
import com.example.model.WaveConfig
import com.example.model.WeaponState
import com.example.model.WeaponType
import com.example.model.Zombie
import com.example.model.ZombieType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

data class GameUiState(
    val status: GameStatus = GameStatus.MENU,
    val wave: Int = 1,
    val score: Int = 0,
    val waveKills: Int = 0,
    val totalRunKills: Int = 0,
    val coins: Int = 50,
    val barricadeMaxHp: Float = 100f,
    val barricadeCurrentHp: Float = 100f,
    val barricadeSpikesLevel: Int = 0, // deals damage to attacking zombies
    val barricadeFlashTimer: Float = 0f,
    val zombiesRemainingToSpawn: Int = 0,
    val totalZombiesInWave: Int = 10,
    val currentBossName: String? = null,
    val currentBossHpRatio: Float = 0f,
    val selectedWeaponIndex: Int = 0,
    val weapons: List<WeaponState> = emptyList(),
    val supportItems: Map<SupportItem, Int> = mapOf(
        SupportItem.GRENADE to 2,
        SupportItem.CRYO_BOMB to 1,
        SupportItem.AIRSTRIKE to 1,
        SupportItem.REPAIR_KIT to 1
    ),
    val waveTitle: String = "¡OLEADA 1!",
    val waveSubtitle: String = "La infección ha comenzado...",
    val intermissionSecondsRemaining: Int = 0,
    val runDurationSeconds: Int = 0,
    val screenShakeIntensity: Float = 0f,
    val comboStreak: Int = 0,
    val aimNormX: Float = 0.5f,
    val aimNormY: Float = 0.35f,
    val muzzleFlashTimer: Float = 0f,
    val soldierNormX: Float = 0.5f,
    val soldierWalkAnim: Float = 0f,
    val isSoldierMoving: Boolean = false,
    val areModernWeaponsDisabled: Boolean = false,
    val isShowingAdMobReward: Boolean = false,
    val adMobCountdown: Int = 5,
    val adMobPendingRewardWeapon: WeaponType? = null,
    val adMobRewardType: String = "",
    val pointsDamageLevel: Int = 1,
    val pointsBarricadeLevel: Int = 1,
    val isPointsUpgradeOpen: Boolean = false,
    val optionsMenuOpen: Boolean = false,
    val feedbackMessage: String? = null
) {
    val globalDamageMultiplier: Float
        get() = 1f + (pointsDamageLevel - 1) * 0.15f

    val barricadeDamageReduction: Float
        get() = ((pointsBarricadeLevel - 1) * 0.08f).coerceAtMost(0.60f)

    val nextDamageUpgradeCost: Int
        get() = 120 * pointsDamageLevel

    val nextBarricadeUpgradeCost: Int
        get() = 100 * pointsBarricadeLevel

    val repairPointsCost: Int
        get() = 80

    val nextSpikesCost: Int
        get() = 140 * (barricadeSpikesLevel + 1)
}

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GameRepository
    val audio: GameAudio
    val adMobManager: AdMobManager

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    // Real-time dynamic game entities
    val activeZombies = mutableListOf<Zombie>()
    val activeProjectiles = mutableListOf<Projectile>()
    val activeParticles = mutableListOf<Particle>()
    val activeDecals = mutableListOf<GroundDecal>()
    val activeShells = mutableListOf<ShellCasing>()
    private var comboTimer: Float = 0f
    private var targetSoldierX: Float = 0.5f

    // State tick flow so Compose canvas updates smoothly
    private val _gameTick = MutableStateFlow(0L)
    val gameTick: StateFlow<Long> = _gameTick.asStateFlow()

    val recentRecords: StateFlow<List<GameRecordEntity>>
    val playerProfile: StateFlow<PlayerProfileEntity?>

    private var gameLoopJob: Job? = null
    private var lastFireTimestamp: Long = 0L
    private var lastSpawnTimestamp: Long = 0L
    private var idCounter: Long = 1000L

    init {
        val database = GameDatabase.getInstance(application)
        repository = GameRepository(database.gameDao())
        audio = GameAudio(application)
        adMobManager = AdMobManager.getInstance(application)

        recentRecords = repository.recentRecords.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        playerProfile = repository.playerProfile.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )

        initializeWeapons()
        loadSavedProfile()
    }

    private fun initializeWeapons() {
        val defaultWeapons = WeaponType.entries.map { type ->
            WeaponState(
                type = type,
                isUnlocked = type == WeaponType.PISTOL,
                upgradeLevel = 1,
                currentAmmo = type.magazineSize
            )
        }
        _uiState.update { it.copy(weapons = defaultWeapons) }
    }

    private fun loadSavedProfile() {
        viewModelScope.launch {
            val profile = repository.getProfileDirect()
            val unlockedSet = profile.unlockedWeapons.split(",").toSet()
            val levelsMap = profile.weaponLevels.split(",")
                .mapNotNull {
                    val parts = it.split(":")
                    if (parts.size == 2) parts[0] to (parts[1].toIntOrNull() ?: 1) else null
                }.toMap()

            _uiState.update { current ->
                val updatedWeapons = current.weapons.map { w ->
                    val isUnlocked = unlockedSet.contains(w.type.id) || w.type == WeaponType.PISTOL
                    val level = levelsMap[w.type.id] ?: 1
                    w.copy(
                        isUnlocked = isUnlocked,
                        upgradeLevel = level,
                        currentAmmo = (w.type.magazineSize * (1f + (level - 1) * 0.20f)).toInt()
                    )
                }
                val maxHp = 100f + (profile.barricadeLevel - 1) * 35f
                current.copy(
                    coins = profile.totalCoins,
                    barricadeMaxHp = maxHp,
                    barricadeCurrentHp = maxHp,
                    barricadeSpikesLevel = (profile.barricadeLevel - 1).coerceAtLeast(0),
                    weapons = updatedWeapons
                )
            }
        }
    }

    fun startGame() {
        activeZombies.clear()
        activeProjectiles.clear()
        activeParticles.clear()
        activeDecals.clear()
        activeShells.clear()

        val maxHp = _uiState.value.barricadeMaxHp
        _uiState.update {
            it.copy(
                status = GameStatus.PLAYING,
                wave = 1,
                score = 0,
                waveKills = 0,
                totalRunKills = 0,
                barricadeCurrentHp = maxHp,
                runDurationSeconds = 0,
                selectedWeaponIndex = 0,
                comboStreak = 0
            )
        }

        // Reset ammo on all weapons
        _uiState.value.weapons.forEach { w ->
            w.currentAmmo = w.maxAmmo
            w.isReloading = false
            w.reloadProgress = 0f
        }

        prepareWave(1)
        startGameLoop()
    }

    fun restartGame() {
        stopGameLoop()
        startGame()
    }

    fun pauseGame() {
        if (_uiState.value.status == GameStatus.PLAYING) {
            _uiState.update { it.copy(status = GameStatus.PAUSED) }
        }
    }

    fun resumeGame() {
        if (_uiState.value.status == GameStatus.PAUSED) {
            _uiState.update { it.copy(status = GameStatus.PLAYING) }
        }
    }

    private fun prepareWave(waveNumber: Int) {
        val totalZombies = 8 + (waveNumber * 4) + (if (waveNumber % 5 == 0) 6 else 0)
        val spawnInterval = (1400L - (waveNumber * 75L)).coerceAtLeast(420L)
        val speedMult = 1f + (waveNumber - 1) * 0.08f
        val hpMult = 1f + (waveNumber - 1) * 0.16f
        val isBossWave = waveNumber % 5 == 0

        val title = if (isBossWave) "¡ALERTA: JEFE MUTANTE!" else "¡OLEADA $waveNumber!"
        val subtitle = when {
            isBossWave -> "Una criatura colosal lidera la horda sanguinaria."
            waveNumber == 1 -> "Elimina a los zombis antes de que alcancen tu defensa."
            waveNumber in 2..4 -> "La velocidad y cantidad de infectados va en aumento."
            waveNumber in 6..9 -> "Horda enfurecida. Tanques blindados detectados."
            else -> "¡Supervivencia extrema! Dispara sin tregua."
        }

        _uiState.update {
            it.copy(
                wave = waveNumber,
                totalZombiesInWave = totalZombies,
                zombiesRemainingToSpawn = totalZombies,
                waveKills = 0,
                waveTitle = title,
                waveSubtitle = subtitle,
                currentBossName = if (isBossWave) "GOLIAT MUTANTE LVL $waveNumber" else null,
                currentBossHpRatio = if (isBossWave) 1f else 0f
            )
        }

        lastSpawnTimestamp = System.currentTimeMillis() + 800L
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            var secondAccumulator = 0f
            val dt = 0.016f // ~60 updates per second

            while (isActive) {
                val currentTime = System.currentTimeMillis()

                if (_uiState.value.status == GameStatus.PLAYING) {
                    secondAccumulator += dt
                    if (secondAccumulator >= 1f) {
                        secondAccumulator -= 1f
                        _uiState.update { it.copy(runDurationSeconds = it.runDurationSeconds + 1) }
                    }

                    // 1. Soldier Movement & Walk Tracking
                    handleSoldierMovement(dt)

                    // 2. Spawning
                    handleSpawning(currentTime)

                    // 3. Zombies update
                    handleZombiesUpdate(dt)

                    // 4. Projectiles & Collisions
                    handleProjectilesUpdate(dt)

                    // 4. Reload progress
                    handleReloads(dt)

                    // 5. Screen shake decay & Barricade flash
                    handleEffectsDecay(dt)

                    // 6. Particles update
                    handleParticlesUpdate(dt)

                    // 7. Check Wave Complete
                    checkWaveCompletion()
                } else if (_uiState.value.status == GameStatus.WAVE_INTERMISSION) {
                    handleIntermission(dt)
                    handleParticlesUpdate(dt)
                }

                _gameTick.value = currentTime
                delay(16L)
            }
        }
    }

    private fun stopGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = null
    }

    private fun handleSpawning(currentTime: Long) {
        val state = _uiState.value
        if (state.zombiesRemainingToSpawn <= 0) return

        val interval = (1300L - (state.wave * 70L)).coerceAtLeast(420L)
        if (currentTime - lastSpawnTimestamp >= interval) {
            lastSpawnTimestamp = currentTime

            val wave = state.wave
            val isBossWave = wave % 5 == 0
            val spawnBoss = isBossWave && state.zombiesRemainingToSpawn == 1 && activeZombies.none { it.type == ZombieType.BOSS }

            val type = if (spawnBoss) {
                ZombieType.BOSS
            } else {
                pickZombieType(wave)
            }

            val hpMultiplier = 1f + (wave - 1) * 0.16f
            val speedMultiplier = 1f + (wave - 1) * 0.075f

            val spawnX = Random.nextFloat() * 0.84f + 0.08f // Spawns across width
            val zombie = Zombie(
                id = idCounter++,
                type = type,
                x = spawnX,
                y = -0.05f,
                maxHp = type.baseHp * hpMultiplier * (if (type == ZombieType.BOSS) 1f + wave * 0.3f else 1f),
                currentHp = type.baseHp * hpMultiplier * (if (type == ZombieType.BOSS) 1f + wave * 0.3f else 1f),
                speed = type.baseSpeed * speedMultiplier,
                damage = type.baseDamage * (1f + wave * 0.05f),
                pointValue = type.pointValue + wave * 10,
                coinValue = type.coinValue + (wave / 2),
                radius = type.radius
            )

            activeZombies.add(zombie)
            _uiState.update { it.copy(zombiesRemainingToSpawn = it.zombiesRemainingToSpawn - 1) }
        }
    }

    private fun pickZombieType(wave: Int): ZombieType {
        val rand = Random.nextFloat()
        return when {
            wave == 1 -> ZombieType.WALKER
            wave == 2 -> if (rand < 0.3f) ZombieType.RUNNER else ZombieType.WALKER
            wave in 3..4 -> when {
                rand < 0.25f -> ZombieType.RUNNER
                rand < 0.40f -> ZombieType.TOXIC
                rand < 0.55f -> ZombieType.BRUTE
                else -> ZombieType.WALKER
            }
            else -> when {
                rand < 0.30f -> ZombieType.RUNNER
                rand < 0.55f -> ZombieType.BRUTE
                rand < 0.75f -> ZombieType.TOXIC
                else -> ZombieType.WALKER
            }
        }
    }

    private fun handleZombiesUpdate(dt: Float) {
        val barricadeY = 0.86f
        val iterator = activeZombies.iterator()

        while (iterator.hasNext()) {
            val zombie = iterator.next()

            // Freeze logic
            if (zombie.isFrozen) {
                zombie.freezeTimer -= dt
                if (zombie.freezeTimer <= 0f) {
                    zombie.isFrozen = false
                }
                continue // don't move or attack while frozen
            }

            // Hit flash timer
            if (zombie.hitFlashTimer > 0f) {
                zombie.hitFlashTimer = (zombie.hitFlashTimer - dt * 5f).coerceAtLeast(0f)
            }

            // Move downward
            if (zombie.y < barricadeY) {
                zombie.y += zombie.speed * dt
                zombie.walkAnim += dt * 8f
                // Slight horizontal wandering wobble
                zombie.x = (zombie.x + sin(zombie.walkAnim * 1.5f) * 0.0006f).coerceIn(0.05f, 0.95f)
            } else {
                // At the barricade: attack!
                zombie.attackCooldown -= dt
                if (zombie.attackCooldown <= 0f) {
                    zombie.attackCooldown = 1.0f // attack once every second
                    damageBarricade(zombie.damage)

                    // Spikes reflect damage
                    val spikes = _uiState.value.barricadeSpikesLevel
                    if (spikes > 0) {
                        val reflectDmg = spikes * 15f
                        zombie.currentHp -= reflectDmg
                        spawnDamageText(zombie.x, zombie.y, "-${reflectDmg.toInt()} ⚡", Color(0xFF00E5FF))
                        if (zombie.currentHp <= 0f) {
                            killZombie(zombie)
                            iterator.remove()
                            continue
                        }
                    }
                }
            }
        }

        // Update Boss HP ratio if boss exists
        val boss = activeZombies.firstOrNull { it.type == ZombieType.BOSS }
        if (boss != null) {
            _uiState.update { it.copy(currentBossHpRatio = (boss.currentHp / boss.maxHp).coerceIn(0f, 1f)) }
        }
    }

    private fun handleProjectilesUpdate(dt: Float) {
        val pIterator = activeProjectiles.iterator()

        while (pIterator.hasNext()) {
            val p = pIterator.next()
            p.x += p.vx * dt
            p.y += p.vy * dt

            // Check if off screen
            if (p.x < -0.1f || p.x > 1.1f || p.y < -0.1f || p.y > 1.1f) {
                pIterator.remove()
                continue
            }

            // Collision check against zombies
            val zIterator = activeZombies.iterator()
            var projectileConsumed = false

            while (zIterator.hasNext()) {
                val zombie = zIterator.next()
                // Convert normalized coordinates to aspect-aware distance
                val dx = (p.x - zombie.x) * 360f
                val dy = (p.y - zombie.y) * 640f
                val dist = sqrt(dx * dx + dy * dy)

                if (dist <= zombie.radius + 6f) {
                    // HIT!
                    audio.playZombieHit()

                    if (p.isExplosive) {
                        detonateExplosion(p.x, p.y, p.explosionRadius, p.damage)
                        projectileConsumed = true
                        break
                    } else {
                        // Headshot detection (upper 35% of zombie hitbox)
                        val isHeadshot = dy < -zombie.radius * 0.28f
                        val actualDamage = if (isHeadshot) p.damage * 1.85f else p.damage

                        zombie.currentHp -= actualDamage
                        zombie.hitFlashTimer = 1f
                        spawnBloodSplatter(zombie.x, zombie.y, if (isHeadshot) Color(0xFFFF1744) else zombie.type.skinColor)

                        if (isHeadshot) {
                            spawnDamageText(zombie.x, zombie.y, "CRÍTICO -${actualDamage.toInt()} 🎯", Color(0xFFFFD54F))
                        } else {
                            spawnDamageText(zombie.x, zombie.y, "-${actualDamage.toInt()}", Color.White)
                        }

                        // Directional Knockback
                        zombie.y = (zombie.y - (if (isHeadshot) 0.024f else 0.014f)).coerceAtLeast(0f)

                        if (zombie.currentHp <= 0f) {
                            killZombie(zombie)
                            zIterator.remove()
                        }

                        p.remainingPierce--
                        if (p.remainingPierce <= 0) {
                            projectileConsumed = true
                            break
                        }
                    }
                }
            }

            if (projectileConsumed) {
                pIterator.remove()
            }
        }
    }

    private fun detonateExplosion(x: Float, y: Float, radiusPixels: Float, damage: Float) {
        audio.playExplosion()
        _uiState.update { it.copy(screenShakeIntensity = 14f) }

        // Persistent Scorch Mark Decal on ground
        activeDecals.add(
            GroundDecal(
                id = idCounter++,
                x = x,
                y = y,
                radius = radiusPixels * 0.38f,
                color = Color(0xDD111111),
                alpha = 0.9f,
                isScorch = true
            )
        )
        if (activeDecals.size > 45) activeDecals.removeAt(0)

        // Shockwave and fireball particles
        for (i in 0 until 24) {
            val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
            val speed = Random.nextFloat() * 0.5f + 0.1f
            activeParticles.add(
                Particle(
                    id = idCounter++,
                    x = x,
                    y = y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    size = Random.nextFloat() * 16f + 8f,
                    color = if (Random.nextBoolean()) Color(0xFFFF3D00) else Color(0xFFFFD600),
                    decayRate = 0.035f
                )
            )
        }

        // Damage zombies in radius
        val zIterator = activeZombies.iterator()
        while (zIterator.hasNext()) {
            val zombie = zIterator.next()
            val dx = (x - zombie.x) * 360f
            val dy = (y - zombie.y) * 640f
            val dist = sqrt(dx * dx + dy * dy)

            if (dist <= radiusPixels + zombie.radius) {
                val falloff = (1f - (dist / (radiusPixels + zombie.radius))).coerceIn(0.4f, 1f)
                val dmg = damage * falloff
                zombie.currentHp -= dmg
                zombie.hitFlashTimer = 1f
                spawnBloodSplatter(zombie.x, zombie.y, Color(0xFFFF1744))
                spawnDamageText(zombie.x, zombie.y, "-${dmg.toInt()} 💥", Color(0xFFFF5252))

                // Heavy knockback from explosion center
                zombie.y = (zombie.y - 0.045f).coerceAtLeast(0f)

                if (zombie.currentHp <= 0f) {
                    killZombie(zombie)
                    zIterator.remove()
                }
            }
        }
    }

    private fun killZombie(zombie: Zombie) {
        comboTimer = 2.5f
        val newCombo = _uiState.value.comboStreak + 1
        val comboMult = 1f + (newCombo * 0.12f).coerceAtMost(3.0f)
        val earnedScore = (zombie.pointValue * comboMult).toInt()
        val earnedCoins = zombie.coinValue

        _uiState.update {
            it.copy(
                score = it.score + earnedScore,
                waveKills = it.waveKills + 1,
                totalRunKills = it.totalRunKills + 1,
                coins = it.coins + earnedCoins,
                comboStreak = newCombo
            )
        }

        spawnCoinText(zombie.x, zombie.y, "+$earnedCoins 🪙")

        if (newCombo >= 3) {
            spawnDamageText(zombie.x, zombie.y - 0.04f, "COMBO x$newCombo 🔥", Color(0xFFFF9100))
        }

        // Persistent Blood Pool Ground Decal
        activeDecals.add(
            GroundDecal(
                id = idCounter++,
                x = zombie.x,
                y = zombie.y,
                radius = zombie.radius * (Random.nextFloat() * 0.4f + 0.9f),
                color = if (zombie.type == ZombieType.TOXIC) Color(0xAA00E676) else Color(0xAA8B0000),
                alpha = 0.85f
            )
        )
        if (activeDecals.size > 45) activeDecals.removeAt(0)

        // Gore explosion death particles
        val particleCount = if (zombie.type.isBoss) 36 else 14
        for (i in 0 until particleCount) {
            val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
            val speed = Random.nextFloat() * 0.35f + 0.06f
            activeParticles.add(
                Particle(
                    id = idCounter++,
                    x = zombie.x,
                    y = zombie.y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    size = Random.nextFloat() * 9f + 4f,
                    color = if (Random.nextBoolean()) Color(0xFFFF1744) else zombie.type.skinColor,
                    decayRate = 0.028f
                )
            )
        }
    }

    private fun damageBarricade(amount: Float) {
        audio.vibrateMedium()
        _uiState.update { current ->
            val reducedDamage = (amount * (1f - current.barricadeDamageReduction)).coerceAtLeast(1f)
            val newHp = (current.barricadeCurrentHp - reducedDamage).coerceAtLeast(0f)
            val updated = current.copy(
                barricadeCurrentHp = newHp,
                barricadeFlashTimer = 1f,
                screenShakeIntensity = 7f
            )
            if (newHp <= 0f) {
                onGameOver()
            }
            updated
        }
    }

    private fun handleReloads(dt: Float) {
        val weapon = _uiState.value.weapons.getOrNull(_uiState.value.selectedWeaponIndex) ?: return
        if (weapon.isReloading) {
            val progressDelta = dt / (weapon.reloadTimeMs / 1000f)
            weapon.reloadProgress += progressDelta
            if (weapon.reloadProgress >= 1f) {
                weapon.isReloading = false
                weapon.reloadProgress = 0f
                weapon.currentAmmo = weapon.maxAmmo
                audio.playReload()
            }
        }
    }

    private fun handleEffectsDecay(dt: Float) {
        if (comboTimer > 0f) {
            comboTimer -= dt
            if (comboTimer <= 0f) {
                _uiState.update { it.copy(comboStreak = 0) }
            }
        }

        // Update flying shell casings
        val shellIterator = activeShells.iterator()
        while (shellIterator.hasNext()) {
            val s = shellIterator.next()
            s.x += s.vx * dt
            s.y += s.vy * dt
            s.rotation += 240f * dt
            s.alpha -= dt * 0.7f
            if (s.alpha <= 0f) {
                shellIterator.remove()
            }
        }

        _uiState.update { current ->
            val newShake = (current.screenShakeIntensity - dt * 30f).coerceAtLeast(0f)
            val newFlash = (current.barricadeFlashTimer - dt * 4f).coerceAtLeast(0f)
            val newMuzzle = (current.muzzleFlashTimer - dt * 9f).coerceAtLeast(0f)
            if (newShake != current.screenShakeIntensity ||
                newFlash != current.barricadeFlashTimer ||
                newMuzzle != current.muzzleFlashTimer
            ) {
                current.copy(
                    screenShakeIntensity = newShake,
                    barricadeFlashTimer = newFlash,
                    muzzleFlashTimer = newMuzzle
                )
            } else {
                current
            }
        }
    }

    private fun handleParticlesUpdate(dt: Float) {
        val iterator = activeParticles.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.life -= p.decayRate
            p.alpha = p.life.coerceIn(0f, 1f)
            if (p.life <= 0f) {
                iterator.remove()
            }
        }
    }

    private fun checkWaveCompletion() {
        val state = _uiState.value
        if (state.zombiesRemainingToSpawn == 0 && activeZombies.isEmpty()) {
            // Wave Cleared!
            audio.playWaveClear()
            val bonusCoins = 30 + state.wave * 15
            val bonusScore = state.wave * 250

            _uiState.update {
                it.copy(
                    status = GameStatus.WAVE_INTERMISSION,
                    intermissionSecondsRemaining = 4,
                    coins = it.coins + bonusCoins,
                    score = it.score + bonusScore,
                    waveTitle = "¡OLEADA ${state.wave} COMPLETADA!",
                    waveSubtitle = "+$bonusCoins Monedas | +$bonusScore Puntos"
                )
            }

            // Confetti / Victory sparks
            for (i in 0 until 24) {
                val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
                val speed = Random.nextFloat() * 0.35f + 0.1f
                activeParticles.add(
                    Particle(
                        id = idCounter++,
                        x = 0.5f,
                        y = 0.4f,
                        vx = cos(angle) * speed,
                        vy = sin(angle) * speed,
                        size = 10f,
                        color = listOf(Color(0xFF00E676), Color(0xFFFFD54F), Color(0xFF00E5FF), Color(0xFFFF1744)).random(),
                        decayRate = 0.02f
                    )
                )
            }
        }
    }

    private var intermissionAccumulator = 0f
    private fun handleIntermission(dt: Float) {
        intermissionAccumulator += dt
        if (intermissionAccumulator >= 1f) {
            intermissionAccumulator -= 1f
            val remaining = _uiState.value.intermissionSecondsRemaining - 1
            if (remaining <= 0) {
                // Begin next wave
                val nextWave = _uiState.value.wave + 1
                _uiState.update { it.copy(status = GameStatus.PLAYING, wave = nextWave) }
                prepareWave(nextWave)
            } else {
                _uiState.update { it.copy(intermissionSecondsRemaining = remaining) }
            }
        }
    }

    fun fireAt(targetNormX: Float, targetNormY: Float) {
        if (_uiState.value.status != GameStatus.PLAYING) return

        val state = _uiState.value
        val weapon = state.weapons.getOrNull(state.selectedWeaponIndex) ?: return

        if (weapon.isReloading) return

        if (weapon.currentAmmo <= 0) {
            reloadCurrentWeapon()
            return
        }

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastFireTimestamp < weapon.fireRateMs) {
            return
        }
        lastFireTimestamp = currentTime

        val currentSoldierX = state.soldierNormX
        targetSoldierX = targetNormX.coerceIn(0.14f, 0.86f)

        // Update aim reticle coordinates, muzzle flash & soldier aim
        _uiState.update {
            it.copy(
                aimNormX = targetNormX,
                aimNormY = targetNormY,
                muzzleFlashTimer = 1f
            )
        }

        // Starting point (soldier's position along the defense line)
        val startX = currentSoldierX
        val startY = 0.84f

        // Eject brass shell casing from soldier's weapon
        activeShells.add(
            ShellCasing(
                id = idCounter++,
                x = startX + 0.02f,
                y = startY + 0.01f,
                vx = Random.nextFloat() * 0.18f + 0.06f,
                vy = Random.nextFloat() * 0.08f - 0.04f,
                rotation = Random.nextFloat() * 360f
            )
        )
        if (activeShells.size > 25) activeShells.removeAt(0)

        // Decrement Ammo
        weapon.currentAmmo--
        if (weapon.currentAmmo <= 0) {
            reloadCurrentWeapon()
        }

        // Play weapon gunshot
        audio.playGunshot(weapon.type.name)

        val baseAngle = atan2(targetNormY - startY, targetNormX - startX)
        val bulletSpeed = 1.4f
        val effectiveDamage = weapon.damage * state.globalDamageMultiplier

        if (weapon.type.pellets > 1) {
            // Shotgun spread
            for (i in 0 until weapon.type.pellets) {
                val spread = (Random.nextFloat() * 2f - 1f) * weapon.type.spreadRad
                val angle = baseAngle + spread
                activeProjectiles.add(
                    Projectile(
                        id = idCounter++,
                        x = startX,
                        y = startY,
                        vx = cos(angle) * bulletSpeed,
                        vy = sin(angle) * bulletSpeed,
                        damage = effectiveDamage,
                        weaponType = weapon.type,
                        remainingPierce = weapon.type.piercingCount,
                        isExplosive = weapon.type.isExplosive,
                        explosionRadius = weapon.type.explosionRadius,
                        color = weapon.type.bulletColor
                    )
                )
            }
        } else {
            // Single shot / Piercing / Explosive
            activeProjectiles.add(
                Projectile(
                    id = idCounter++,
                    x = startX,
                    y = startY,
                    vx = cos(baseAngle) * bulletSpeed,
                    vy = sin(baseAngle) * bulletSpeed,
                    damage = effectiveDamage,
                    weaponType = weapon.type,
                    remainingPierce = weapon.type.piercingCount,
                    isExplosive = weapon.type.isExplosive,
                    explosionRadius = weapon.type.explosionRadius,
                    color = weapon.type.bulletColor
                )
            )
        }

        // Muzzle flash particle
        activeParticles.add(
            Particle(
                id = idCounter++,
                x = startX + cos(baseAngle) * 0.04f,
                y = startY + sin(baseAngle) * 0.04f,
                vx = 0f,
                vy = 0f,
                size = 18f,
                color = Color(0xFFFFF176),
                decayRate = 0.15f
            )
        )
    }

    fun selectWeapon(index: Int) {
        val weapon = _uiState.value.weapons.getOrNull(index) ?: return
        if (weapon.isUnlocked) {
            _uiState.update { it.copy(selectedWeaponIndex = index) }
        }
    }

    fun reloadCurrentWeapon() {
        val weapon = _uiState.value.weapons.getOrNull(_uiState.value.selectedWeaponIndex) ?: return
        if (!weapon.isReloading && weapon.currentAmmo < weapon.maxAmmo) {
            weapon.isReloading = true
            weapon.reloadProgress = 0f
            audio.playReload()
        }
    }

    fun useSupportItem(item: SupportItem) {
        val currentCount = _uiState.value.supportItems[item] ?: 0
        if (currentCount <= 0) return

        _uiState.update {
            it.copy(
                supportItems = it.supportItems.toMutableMap().apply {
                    put(item, currentCount - 1)
                }
            )
        }

        when (item) {
            SupportItem.GRENADE -> {
                detonateExplosion(0.5f, 0.45f, 180f, 220f)
            }
            SupportItem.CRYO_BOMB -> {
                audio.playExplosion()
                activeZombies.forEach {
                    it.isFrozen = true
                    it.freezeTimer = 4.0f
                }
                // Ice particles
                for (i in 0 until 25) {
                    val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
                    val speed = Random.nextFloat() * 0.4f + 0.1f
                    activeParticles.add(
                        Particle(
                            id = idCounter++,
                            x = 0.5f,
                            y = 0.4f,
                            vx = cos(angle) * speed,
                            vy = sin(angle) * speed,
                            size = 12f,
                            color = Color(0xFF80DEEA),
                            decayRate = 0.03f
                        )
                    )
                }
            }
            SupportItem.AIRSTRIKE -> {
                audio.playExplosion()
                _uiState.update { it.copy(screenShakeIntensity = 18f) }
                // Bomb 4 distinct spots
                val targets = listOf(0.2f to 0.25f, 0.8f to 0.3f, 0.5f to 0.5f, 0.35f to 0.65f)
                targets.forEach { (tx, ty) ->
                    detonateExplosion(tx, ty, 160f, 280f)
                }
            }
            SupportItem.REPAIR_KIT -> {
                audio.playReload()
                _uiState.update {
                    val healed = (it.barricadeCurrentHp + it.barricadeMaxHp * 0.4f).coerceAtMost(it.barricadeMaxHp)
                    it.copy(barricadeCurrentHp = healed)
                }
                spawnDamageText(0.5f, 0.85f, "+40% SALUD 🛠️", Color(0xFF00E676))
            }
        }
    }

    // Shop purchase & upgrade functions
    fun buyWeapon(weaponType: WeaponType): Boolean {
        val state = _uiState.value
        if (state.coins < weaponType.basePrice) return false

        val weapon = state.weapons.firstOrNull { it.type == weaponType } ?: return false
        if (weapon.isUnlocked) return false

        weapon.isUnlocked = true
        _uiState.update { it.copy(coins = it.coins - weaponType.basePrice) }
        saveProgressionToDb()
        return true
    }

    fun upgradeWeapon(weaponType: WeaponType): Boolean {
        val weapon = _uiState.value.weapons.firstOrNull { it.type == weaponType } ?: return false
        if (weapon.upgradeLevel >= 5) return false

        val cost = weapon.nextUpgradeCost
        if (_uiState.value.coins < cost) return false

        weapon.upgradeLevel++
        weapon.currentAmmo = weapon.maxAmmo
        _uiState.update { it.copy(coins = it.coins - cost) }
        saveProgressionToDb()
        return true
    }

    fun upgradeBarricade(): Boolean {
        val currentMax = _uiState.value.barricadeMaxHp
        val cost = 90 + ((currentMax - 100f) / 35f * 60).toInt()
        if (_uiState.value.coins < cost) return false

        val newMax = currentMax + 35f
        _uiState.update {
            it.copy(
                coins = it.coins - cost,
                barricadeMaxHp = newMax,
                barricadeCurrentHp = (it.barricadeCurrentHp + 35f).coerceAtMost(newMax),
                barricadeSpikesLevel = it.barricadeSpikesLevel + 1
            )
        }
        saveProgressionToDb()
        return true
    }

    fun buySupportItem(item: SupportItem): Boolean {
        if (_uiState.value.coins < item.cost) return false

        _uiState.update {
            val count = it.supportItems[item] ?: 0
            it.copy(
                coins = it.coins - item.cost,
                supportItems = it.supportItems.toMutableMap().apply {
                    put(item, count + 1)
                }
            )
        }
        return true
    }

    private fun onGameOver() {
        stopGameLoop()
        audio.playGameOver()

        val state = _uiState.value
        _uiState.update { it.copy(status = GameStatus.GAME_OVER) }

        viewModelScope.launch {
            repository.saveGameRun(
                wave = state.wave,
                score = state.score,
                kills = state.totalRunKills,
                durationSeconds = state.runDurationSeconds
            )
            repository.updateCoins(0) // sync coins
            saveProgressionToDb()
        }
    }

    private fun saveProgressionToDb() {
        viewModelScope.launch {
            val profile = repository.getProfileDirect()
            val unlockedString = _uiState.value.weapons.filter { it.isUnlocked }.joinToString(",") { it.type.id }
            val levelsString = _uiState.value.weapons.joinToString(",") { "${it.type.id}:${it.upgradeLevel}" }
            val barricadeLvl = 1 + ((_uiState.value.barricadeMaxHp - 100f) / 35f).toInt()

            repository.updateProfile(
                profile.copy(
                    totalCoins = _uiState.value.coins,
                    unlockedWeapons = unlockedString,
                    weaponLevels = levelsString,
                    barricadeLevel = barricadeLvl
                )
            )
        }
    }

    private fun spawnBloodSplatter(x: Float, y: Float, color: Color) {
        for (i in 0 until 5) {
            val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
            val speed = Random.nextFloat() * 0.15f + 0.05f
            activeParticles.add(
                Particle(
                    id = idCounter++,
                    x = x,
                    y = y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    size = Random.nextFloat() * 6f + 3f,
                    color = color,
                    decayRate = 0.05f
                )
            )
        }
    }

    private fun handleSoldierMovement(dt: Float) {
        val currentX = _uiState.value.soldierNormX
        val diffX = targetSoldierX - currentX
        if (kotlin.math.abs(diffX) > 0.003f) {
            val speed = 0.16f
            val step = (diffX * speed).coerceIn(-0.026f, 0.026f)
            val nextX = (currentX + step).coerceIn(0.12f, 0.88f)
            val walkDelta = kotlin.math.abs(step) * 55f
            _uiState.update {
                it.copy(
                    soldierNormX = nextX,
                    soldierWalkAnim = it.soldierWalkAnim + walkDelta,
                    isSoldierMoving = true
                )
            }
        } else {
            if (_uiState.value.isSoldierMoving) {
                _uiState.update { it.copy(isSoldierMoving = false) }
            }
        }
    }

    fun moveSoldier(targetX: Float) {
        targetSoldierX = targetX.coerceIn(0.12f, 0.88f)
    }

    fun aimAt(targetNormX: Float, targetNormY: Float) {
        targetSoldierX = targetNormX.coerceIn(0.14f, 0.86f)
        _uiState.update {
            it.copy(
                aimNormX = targetNormX,
                aimNormY = targetNormY
            )
        }
    }

    private var adMobJob: Job? = null

    fun triggerAdMobReward(rewardType: String, weapon: WeaponType? = null, activity: Activity? = null) {
        if (activity != null) {
            val isVip = (rewardType == "UNLOCK_WEAPON" || rewardType.contains("PRESTAME") || rewardType.contains("RECHARGE"))
            val shown = adMobManager.showRewardedAd(
                activity = activity,
                isVip = isVip,
                onUserEarnedReward = {
                    _uiState.update {
                        it.copy(
                            adMobRewardType = rewardType,
                            adMobPendingRewardWeapon = weapon
                        )
                    }
                    onAdMobRewardCompleted()
                },
                onDismiss = {
                    // preloading handled automatically in AdMobManager
                }
            )
            if (shown) {
                return
            }
        }

        // Fallback to interactive countdown dialog
        adMobJob?.cancel()
        _uiState.update {
            it.copy(
                isShowingAdMobReward = true,
                adMobCountdown = 5,
                adMobRewardType = rewardType,
                adMobPendingRewardWeapon = weapon
            )
        }

        adMobJob = viewModelScope.launch {
            for (sec in 4 downTo 0) {
                delay(1000L)
                _uiState.update { it.copy(adMobCountdown = sec) }
            }
        }
    }

    fun rechargeScoreWithAdMob(activity: Activity? = null) {
        triggerAdMobReward("RECHARGE_SCORE_AND_COINS", activity = activity)
    }

    fun showWaveInterstitial(activity: Activity?) {
        activity?.let {
            adMobManager.showInterstitialAd(it, InterstitialType.WAVE_CLEAR)
        }
    }

    fun showGameOverInterstitial(activity: Activity?) {
        activity?.let {
            adMobManager.showInterstitialAd(it, InterstitialType.GAME_OVER)
        }
    }

    fun showShopInterstitial(activity: Activity?) {
        activity?.let {
            adMobManager.showInterstitialAd(it, InterstitialType.SHOP)
        }
    }

    fun showReviveInterstitial(activity: Activity?) {
        activity?.let {
            adMobManager.showInterstitialAd(it, InterstitialType.REVIVE)
        }
    }

    fun claimAdMobReward() {
        if (_uiState.value.adMobCountdown == 0) {
            onAdMobRewardCompleted()
        }
    }

    fun dismissAdMob() {
        if (_uiState.value.adMobCountdown == 0) {
            onAdMobRewardCompleted()
        } else {
            adMobJob?.cancel()
            adMobJob = null
            _uiState.update { it.copy(isShowingAdMobReward = false) }
        }
    }

    private fun onAdMobRewardCompleted() {
        val rewardType = _uiState.value.adMobRewardType
        val weapon = _uiState.value.adMobPendingRewardWeapon

        audio.playWaveClear()

        when (rewardType) {
            "UNLOCK_WEAPON" -> {
                if (weapon != null) {
                    val wState = _uiState.value.weapons.firstOrNull { it.type == weapon }
                    if (wState != null) {
                        wState.isUnlocked = true
                        wState.unlockedViaAd = true
                        wState.currentAmmo = wState.maxAmmo
                        wState.isReloading = false
                    }
                }
                _uiState.update {
                    it.copy(
                        isShowingAdMobReward = false,
                        coins = it.coins + 200,
                        score = it.score + 1000
                    )
                }
                saveProgressionToDb()
                spawnDamageText(0.5f, 0.85f, "¡${weapon?.displayName ?: "ARMA"} DESBLOQUEADA!", Color(0xFF00E676))
            }
            "RECHARGE_SCORE", "RECHARGE_SCORE_AND_COINS" -> {
                _uiState.value.weapons.forEach {
                    it.currentAmmo = it.maxAmmo
                    it.isReloading = false
                    it.reloadProgress = 0f
                }
                _uiState.update {
                    it.copy(
                        isShowingAdMobReward = false,
                        coins = it.coins + 300,
                        score = it.score + 1500
                    )
                }
                saveProgressionToDb()
                spawnDamageText(0.5f, 0.85f, "+1,500 PUNTOS | +300 🪙 | MUNICIÓN 100%", Color(0xFFFFD54F))
            }
            "REFILL_AMMO" -> {
                _uiState.value.weapons.forEach {
                    it.currentAmmo = it.maxAmmo
                    it.isReloading = false
                    it.reloadProgress = 0f
                }
                _uiState.update {
                    it.copy(
                        isShowingAdMobReward = false,
                        coins = it.coins + 200,
                        score = it.score + 800
                    )
                }
                spawnDamageText(0.5f, 0.85f, "¡MUNICIÓN COMPLETA +800 PTS!", Color(0xFF00E5FF))
            }
            else -> {
                _uiState.update {
                    it.copy(
                        isShowingAdMobReward = false,
                        coins = it.coins + 250,
                        score = it.score + 1000
                    )
                }
                saveProgressionToDb()
                spawnDamageText(0.5f, 0.85f, "+1,000 PUNTOS + 250 🪙", Color(0xFF00E676))
            }
        }
    }

    fun instantReloadWithScore(): Boolean {
        val scoreCost = 80
        val state = _uiState.value
        val weapon = state.weapons.getOrNull(state.selectedWeaponIndex) ?: return false

        if (state.score < scoreCost) return false
        if (weapon.currentAmmo >= weapon.maxAmmo && !weapon.isReloading) return false

        weapon.currentAmmo = weapon.maxAmmo
        weapon.isReloading = false
        weapon.reloadProgress = 0f
        audio.playReload()

        _uiState.update { it.copy(score = it.score - scoreCost) }
        spawnDamageText(state.soldierNormX, 0.82f, "RECARGA RÁPIDA (-$scoreCost PTS) ⚡", Color(0xFF00E676))
        return true
    }

    fun exchangeScoreForCoins(scoreCost: Int = 300, coinsEarned: Int = 100): Boolean {
        val state = _uiState.value
        if (state.score < scoreCost) return false
        _uiState.update {
            it.copy(
                score = it.score - scoreCost,
                coins = it.coins + coinsEarned
            )
        }
        saveProgressionToDb()
        audio.playWaveClear()
        spawnCoinText(state.soldierNormX, 0.82f, "+$coinsEarned 🪙 (-$scoreCost PTS)")
        return true
    }

    fun toggleModernWeaponsDisabled() {
        val willBeDisabled = !_uiState.value.areModernWeaponsDisabled
        _uiState.update { current ->
            val updatedWeapons = current.weapons.map { w ->
                if (w.type.isModernAdvanced) {
                    if (willBeDisabled) {
                        w.copy(isUnlocked = false)
                    } else {
                        w.copy(isUnlocked = w.unlockedViaAd)
                    }
                } else {
                    w
                }
            }
            val currentSelectedIsModern = current.weapons.getOrNull(current.selectedWeaponIndex)?.type?.isModernAdvanced == true
            val newSelectedIndex = if (willBeDisabled && currentSelectedIsModern) 0 else current.selectedWeaponIndex

            current.copy(
                areModernWeaponsDisabled = willBeDisabled,
                weapons = updatedWeapons,
                selectedWeaponIndex = newSelectedIndex
            )
        }
        val msg = if (willBeDisabled) "ARMAS MODERNAS: DESHABILITADAS" else "ARMAS MODERNAS: HABILITADAS"
        val color = if (willBeDisabled) Color(0xFFFF9100) else Color(0xFF00E676)
        spawnDamageText(_uiState.value.soldierNormX, 0.82f, msg, color)
    }

    fun openPointsUpgradeMenu() {
        _uiState.update { it.copy(isPointsUpgradeOpen = true) }
    }

    fun closePointsUpgradeMenu() {
        _uiState.update { it.copy(isPointsUpgradeOpen = false, feedbackMessage = null) }
    }

    fun toggleOptionsMenu() {
        _uiState.update { it.copy(optionsMenuOpen = !it.optionsMenuOpen) }
    }

    fun redeemPointsForDamage() {
        val state = _uiState.value
        val cost = state.nextDamageUpgradeCost
        if (state.score >= cost && state.pointsDamageLevel < 10) {
            audio.playUpgrade()
            _uiState.update { current ->
                val nextLvl = current.pointsDamageLevel + 1
                current.copy(
                    score = current.score - cost,
                    pointsDamageLevel = nextLvl,
                    feedbackMessage = "¡Potencia aumentada a Nivel $nextLvl! (+${(nextLvl - 1) * 15}% Daño Global)"
                )
            }
            spawnDamageText(_uiState.value.soldierNormX, 0.70f, "+15% DAÑO BALÍSTICO 🔥", Color(0xFFFFD54F))
        }
    }

    fun redeemPointsForBarricade() {
        val state = _uiState.value
        val cost = state.nextBarricadeUpgradeCost
        if (state.score >= cost && state.pointsBarricadeLevel < 10) {
            audio.playUpgrade()
            _uiState.update { current ->
                val nextLvl = current.pointsBarricadeLevel + 1
                val newMaxHp = 100f + (nextLvl - 1) * 35f
                val newHp = (current.barricadeCurrentHp + 35f).coerceAtMost(newMaxHp)
                current.copy(
                    score = current.score - cost,
                    pointsBarricadeLevel = nextLvl,
                    barricadeMaxHp = newMaxHp,
                    barricadeCurrentHp = newHp,
                    feedbackMessage = "¡Blindaje aumentado a Nivel $nextLvl! (+${(nextLvl - 1) * 8}% Resistencia & +35 HP)"
                )
            }
            spawnDamageText(_uiState.value.soldierNormX, 0.78f, "+35 HP & BLINDAJE MEJORADO 🛡️", Color(0xFF00E676))
        }
    }

    fun redeemPointsForRepair() {
        val state = _uiState.value
        val cost = state.repairPointsCost
        if (state.score >= cost && state.barricadeCurrentHp < state.barricadeMaxHp) {
            audio.playRepair()
            _uiState.update { current ->
                val newHp = (current.barricadeCurrentHp + 45f).coerceAtMost(current.barricadeMaxHp)
                current.copy(
                    score = current.score - cost,
                    barricadeCurrentHp = newHp,
                    feedbackMessage = "¡Reparación estructural completada! (+45 HP)"
                )
            }
            spawnDamageText(_uiState.value.soldierNormX, 0.82f, "+45 HP REPARADO 🛠️", Color(0xFF64B5F6))
        }
    }

    fun redeemPointsForSpikes() {
        val state = _uiState.value
        val cost = state.nextSpikesCost
        if (state.score >= cost && state.barricadeSpikesLevel < 5) {
            audio.playUpgrade()
            _uiState.update { current ->
                val nextLvl = current.barricadeSpikesLevel + 1
                current.copy(
                    score = current.score - cost,
                    barricadeSpikesLevel = nextLvl,
                    feedbackMessage = "¡Púas Electrificadas Nivel $nextLvl instaladas! (${nextLvl * 18} daño represalia)"
                )
            }
            spawnDamageText(_uiState.value.soldierNormX, 0.82f, "PÚAS ELÉCTRICAS MEJORADAS ⚡", Color(0xFF00E5FF))
        }
    }

    private fun spawnDamageText(x: Float, y: Float, text: String, color: Color) {
        activeParticles.add(
            Particle(
                id = idCounter++,
                x = x,
                y = y - 0.02f,
                vx = 0f,
                vy = -0.08f,
                size = 14f,
                color = color,
                decayRate = 0.03f,
                text = text
            )
        )
    }

    private fun spawnCoinText(x: Float, y: Float, text: String) {
        activeParticles.add(
            Particle(
                id = idCounter++,
                x = x,
                y = y - 0.03f,
                vx = 0f,
                vy = -0.06f,
                size = 15f,
                color = Color(0xFFFFD54F),
                decayRate = 0.025f,
                text = text
            )
        )
    }
}
