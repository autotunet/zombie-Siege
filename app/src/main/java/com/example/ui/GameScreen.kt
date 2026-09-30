package com.example.ui

import android.app.Activity
import androidx.compose.ui.platform.LocalContext
import com.example.ads.AdMobConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameStatus
import com.example.model.SupportItem
import com.example.model.WeaponState
import com.example.model.WeaponType
import com.example.viewmodel.GameUiState
import com.example.viewmodel.GameViewModel

@Composable
fun GameScreen(viewModel: GameViewModel) {
    val context = LocalContext.current
    val activity = context as? Activity

    val uiState by viewModel.uiState.collectAsState()
    val gameTick by viewModel.gameTick.collectAsState()
    val records by viewModel.recentRecords.collectAsState()
    val profile by viewModel.playerProfile.collectAsState()

    var showShopDialog by remember { mutableStateOf(false) }
    var showRecordsDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
    ) {
        when (uiState.status) {
            GameStatus.MENU -> {
                MainMenuContent(
                    uiState = uiState,
                    profile = profile,
                    audio = viewModel.audio,
                    onStartGame = { viewModel.startGame() },
                    onOpenShop = {
                        viewModel.showShopInterstitial(activity)
                        showShopDialog = true
                    },
                    onOpenPointsUpgrade = { viewModel.openPointsUpgradeMenu() },
                    onOpenRecords = { showRecordsDialog = true }
                )
            }
            GameStatus.PLAYING,
            GameStatus.WAVE_INTERMISSION,
            GameStatus.PAUSED,
            GameStatus.GAME_OVER -> {
                // Active Game Arena Screen
                Box(modifier = Modifier.fillMaxSize()) {
                    // 1. Interactive Combat Canvas
                    GameCanvas(
                        zombies = viewModel.activeZombies,
                        projectiles = viewModel.activeProjectiles,
                        particles = viewModel.activeParticles,
                        decals = viewModel.activeDecals,
                        shells = viewModel.activeShells,
                        barricadeCurrentHp = uiState.barricadeCurrentHp,
                        barricadeMaxHp = uiState.barricadeMaxHp,
                        barricadeFlashTimer = uiState.barricadeFlashTimer,
                        barricadeSpikesLevel = uiState.barricadeSpikesLevel,
                        screenShakeIntensity = uiState.screenShakeIntensity,
                        aimNormX = uiState.aimNormX,
                        aimNormY = uiState.aimNormY,
                        muzzleFlashTimer = uiState.muzzleFlashTimer,
                        comboStreak = uiState.comboStreak,
                        soldierNormX = uiState.soldierNormX,
                        soldierWalkAnim = uiState.soldierWalkAnim,
                        selectedWeaponType = uiState.weapons.getOrNull(uiState.selectedWeaponIndex)?.type ?: WeaponType.PISTOL,
                        onShootTarget = { nx, ny -> viewModel.fireAt(nx, ny) },
                        modifier = Modifier.fillMaxSize()
                    )

                    // 2. Top Game HUD
                    TopGameHud(
                        uiState = uiState,
                        onPause = { viewModel.pauseGame() },
                        onOpenShop = { showShopDialog = true },
                        onOpenPointsUpgrade = { viewModel.openPointsUpgradeMenu() },
                        onRechargeScore = { viewModel.rechargeScoreWithAdMob() },
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .statusBarsPadding()
                    )

                    // 3. Boss Health Bar (if active)
                    if (uiState.currentBossName != null && uiState.currentBossHpRatio > 0f) {
                        BossHealthGauge(
                            name = uiState.currentBossName ?: "",
                            hpRatio = uiState.currentBossHpRatio,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 96.dp)
                                .statusBarsPadding()
                        )
                    }

                    // 4. Wave Announcement / Intermission Banner
                    AnimatedVisibility(
                        visible = uiState.status == GameStatus.WAVE_INTERMISSION,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut(),
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        WaveIntermissionBanner(
                            uiState = uiState,
                            onOpenShop = { showShopDialog = true },
                            onOpenPointsUpgrade = { viewModel.openPointsUpgradeMenu() }
                        )
                    }

                    // 5. Bottom Tactical Controls (Weapons & Support abilities)
                    BottomTacticalDeck(
                        uiState = uiState,
                        onSelectWeapon = { viewModel.selectWeapon(it) },
                        onReload = { viewModel.reloadCurrentWeapon() },
                        onInstantReloadWithScore = { viewModel.instantReloadWithScore() },
                        onUnlockWithAdMob = { viewModel.triggerAdMobReward("UNLOCK_WEAPON", it) },
                        onUseSupport = { viewModel.useSupportItem(it) },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding()
                    )
                }
            }
            GameStatus.VICTORY -> {}
        }

        // Dialogs
        if (uiState.status == GameStatus.PAUSED) {
            PauseDialog(
                onResume = { viewModel.resumeGame() },
                onRestart = { viewModel.restartGame() },
                audio = viewModel.audio
            )
        }

        if (uiState.status == GameStatus.GAME_OVER) {
            GameOverDialog(
                uiState = uiState,
                onRestart = {
                    viewModel.showGameOverInterstitial(activity)
                    viewModel.restartGame()
                },
                onOpenShop = {
                    viewModel.showShopInterstitial(activity)
                    showShopDialog = true
                },
                onReviveWithAd = {
                    viewModel.triggerAdMobReward("RECHARGE_SCORE_AND_COINS", activity = activity)
                    viewModel.resumeGame()
                }
            )
        }

        if (showShopDialog) {
            ShopDialog(
                uiState = uiState,
                onDismiss = {
                    viewModel.showShopInterstitial(activity)
                    showShopDialog = false
                },
                onBuyWeapon = { viewModel.buyWeapon(it) },
                onUpgradeWeapon = { viewModel.upgradeWeapon(it) },
                onUpgradeBarricade = { viewModel.upgradeBarricade() },
                onBuySupportItem = { viewModel.buySupportItem(it) },
                onWatchAdToUnlock = { viewModel.triggerAdMobReward("UNLOCK_WEAPON", it, activity = activity) },
                onToggleModernWeapons = { viewModel.toggleModernWeaponsDisabled() },
                onExchangeScoreForCoins = { viewModel.exchangeScoreForCoins() },
                onRechargeScoreWithAdMob = { viewModel.rechargeScoreWithAdMob(activity = activity) }
            )
        }

        if (showRecordsDialog) {
            RecordsDialog(
                profile = profile,
                records = records,
                onDismiss = { showRecordsDialog = false }
            )
        }

        if (uiState.isShowingAdMobReward) {
            AdMobRewardDialog(
                countdownSeconds = uiState.adMobCountdown,
                rewardType = uiState.adMobRewardType,
                rewardWeapon = uiState.adMobPendingRewardWeapon,
                onClaimReward = { viewModel.claimAdMobReward() },
                onDismiss = { viewModel.dismissAdMob() }
            )
        }

        if (uiState.isPointsUpgradeOpen) {
            PointsUpgradeDialog(
                uiState = uiState,
                onUpgradeDamage = { viewModel.redeemPointsForDamage() },
                onUpgradeBarricade = { viewModel.redeemPointsForBarricade() },
                onRepairBarricade = { viewModel.redeemPointsForRepair() },
                onUpgradeSpikes = { viewModel.redeemPointsForSpikes() },
                onDismiss = { viewModel.closePointsUpgradeMenu() }
            )
        }
    }
}

@Composable
private fun MainMenuContent(
    uiState: GameUiState,
    profile: com.example.data.PlayerProfileEntity?,
    audio: com.example.audio.GameAudio,
    onStartGame: () -> Unit,
    onOpenShop: () -> Unit,
    onOpenPointsUpgrade: () -> Unit,
    onOpenRecords: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Top Bar: Coins & Stats (Centered and balanced)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF161B22))
                            .border(1.dp, Color(0xFFFFD54F), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${uiState.coins} 🪙",
                            color = Color(0xFFFFD54F),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF161B22))
                            .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(12.dp))
                            .clickable { onOpenPointsUpgrade() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("menu_points_chip")
                    ) {
                        Text(
                            text = "${uiState.score} ⭐",
                            color = Color(0xFF00E5FF),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { audio.soundEnabled = !audio.soundEnabled },
                        modifier = Modifier.testTag("menu_sound_toggle")
                    ) {
                        Text(if (audio.soundEnabled) "🔊" else "🔇", fontSize = 18.sp)
                    }

                    IconButton(
                        onClick = onOpenRecords,
                        modifier = Modifier.testTag("menu_records_button")
                    ) {
                        Icon(
                            Icons.Default.EmojiEvents,
                            contentDescription = "Récords",
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Title and Hero Art (Centered)
            Text(text = "🧟‍♂️", fontSize = 64.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "ZOMBIE SIEGE",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                ),
                color = Color(0xFF00E676),
                textAlign = TextAlign.Center
            )

            Text(
                text = "OUTBREAK DEFENSE",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp
                ),
                color = Color(0xFFFF1744),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tactical Quarantine Alert Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF261010))
                    .border(1.dp, Color(0xFFFF1744), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "⚠️ SECTOR URBANO CUARENTENADO // CÓDIGO NEGRO",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF5252),
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Defiende las calles urbanas de oleadas incesantes de zombis. Utiliza tu arsenal táctico, apunta a la cabeza y resiste la invasión.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF9EABB8),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Best Stats (Centered)
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    QuickStatBadge(label = "Mejor Ola", value = "Ola ${profile?.highestWave ?: 1}")
                }
                Box(modifier = Modifier.weight(1f)) {
                    QuickStatBadge(label = "Récord", value = "${profile?.highestScore ?: 0} pts")
                }
                Box(modifier = Modifier.weight(1f)) {
                    QuickStatBadge(label = "Bajas", value = "${profile?.lifetimeKills ?: 0} 💀")
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Centralized Action Buttons Stack
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onStartGame,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("start_game_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00E676),
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "INICIAR SUPERVIVENCIA",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                }

                Button(
                    onClick = onOpenPointsUpgrade,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("menu_points_upgrade_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E293B),
                        contentColor = Color(0xFFFFD54F)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFD54F)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CANJEAR PUNTOS (DAÑO / BLINDAJE)",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                }

                OutlinedButton(
                    onClick = onOpenShop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("menu_shop_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF64B5F6))
                ) {
                    Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ARMERÍA & TIENDA TÁCTICA", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Standard AdMob Banner Centered
            AdMobBanner(
                adUnitId = AdMobConstants.BANNER_STANDARD,
                isVip = false,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun QuickStatBadge(label: String, value: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF161B22))
            .border(1.dp, Color(0xFF30363D), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label, fontSize = 10.sp, color = Color(0xFF8B949E))
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
private fun TopGameHud(
    uiState: GameUiState,
    onPause: () -> Unit,
    onOpenShop: () -> Unit,
    onOpenPointsUpgrade: () -> Unit,
    onRechargeScore: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Row 1: Wave, Combo, Points / Upgrade, Coins, Pause
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Wave Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF21262D))
                        .border(1.dp, Color(0xFF00E676), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "OLA ${uiState.wave}",
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF00E676),
                        fontSize = 13.sp
                    )
                }

                if (uiState.comboStreak >= 2) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFBF360C))
                            .border(1.dp, Color(0xFFFF9100), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "x${uiState.comboStreak} 🔥",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFFD54F)
                        )
                    }
                }
            }

            // Centralized Points Chip with Quick Upgrade Trigger
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E2638))
                    .border(1.dp, Color(0xFFFFD54F), RoundedCornerShape(12.dp))
                    .clickable { onOpenPointsUpgrade() }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
                    .testTag("hud_points_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${uiState.score} ⭐",
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFFD54F),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF00E676))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "MEJORAR",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        )
                    }
                }
            }

            // Coins & Buttons
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF21262D))
                        .clickable { onOpenShop() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${uiState.coins} 🪙",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD54F),
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = onPause,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF21262D))
                        .testTag("game_pause_button")
                ) {
                    Icon(
                        Icons.Default.Pause,
                        contentDescription = "Pausar",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Row 2: Barricade Health & Horde Progress
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Barricade Gauge
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Shield,
                            contentDescription = null,
                            tint = if (uiState.barricadeCurrentHp < uiState.barricadeMaxHp * 0.3f) Color(0xFFFF1744) else Color(0xFF00E676),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Muro de Defensa",
                            fontSize = 11.sp,
                            color = Color(0xFF8B949E),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = "${uiState.barricadeCurrentHp.toInt()}/${uiState.barricadeMaxHp.toInt()} HP",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                LinearProgressIndicator(
                    progress = { (uiState.barricadeCurrentHp / uiState.barricadeMaxHp).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (uiState.barricadeCurrentHp < uiState.barricadeMaxHp * 0.3f) Color(0xFFFF1744) else Color(0xFF00E676),
                    trackColor = Color(0xFF21262D)
                )
            }

            // Horde Zombies Progress
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Horda Zombi", fontSize = 11.sp, color = Color(0xFF8B949E))
                    Text(
                        text = "Restan: ${uiState.zombiesRemainingToSpawn}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF9100)
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                val spawnedRatio = 1f - (uiState.zombiesRemainingToSpawn.toFloat() / uiState.totalZombiesInWave.coerceAtLeast(1))
                LinearProgressIndicator(
                    progress = { spawnedRatio.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color(0xFFFF9100),
                    trackColor = Color(0xFF21262D)
                )
            }
        }
    }
}

@Composable
private fun BossHealthGauge(
    name: String,
    hpRatio: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(0.85f)
            .border(1.dp, Color(0xFFFF1744), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xCC161B22)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚠️ $name",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                    color = Color(0xFFFF1744),
                    fontSize = 12.sp
                )
                Text(
                    text = "${(hpRatio * 100).toInt()}%",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { hpRatio },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFFFF1744),
                trackColor = Color(0xFF30363D)
            )
        }
    }
}

@Composable
private fun WaveIntermissionBanner(
    uiState: GameUiState,
    onOpenShop: () -> Unit,
    onOpenPointsUpgrade: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .border(2.dp, Color(0xFF00E676), RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFA161B22)),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "🎉", fontSize = 36.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = uiState.waveTitle,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                color = Color(0xFF00E676),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = uiState.waveSubtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFFFD54F),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Siguiente oleada en ${uiState.intermissionSecondsRemaining}s...",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF8B949E)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onOpenPointsUpgrade,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E293B),
                        contentColor = Color(0xFFFFD54F)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD54F)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("intermission_points_btn")
                ) {
                    Text("⭐ CANJEAR", fontWeight = FontWeight.Black, fontSize = 11.sp)
                }

                Button(
                    onClick = onOpenShop,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF238636),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("intermission_shop_button")
                ) {
                    Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ARMERÍA", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun BottomTacticalDeck(
    uiState: GameUiState,
    onSelectWeapon: (Int) -> Unit,
    onReload: () -> Unit,
    onInstantReloadWithScore: () -> Unit,
    onUnlockWithAdMob: (WeaponType) -> Unit,
    onUseSupport: (SupportItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        // Row 1: Support Tactical Abilities (Grenades, Cryo, Airstrike, Repair)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SupportItem.entries.forEach { item ->
                val count = uiState.supportItems[item] ?: 0
                SupportQuickButton(
                    item = item,
                    count = count,
                    onClick = { onUseSupport(item) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Row 2: Weapons Selector Carousel, Quick Score Reload & Standard Reload Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Scrollable weapon list
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for ((index, weapon) in uiState.weapons.withIndex()) {
                    val isAvailable = weapon.isUnlocked && (!weapon.type.isModernAdvanced || !uiState.areModernWeaponsDisabled)
                    if (isAvailable) {
                        val isSelected = index == uiState.selectedWeaponIndex
                        WeaponItemTab(
                            weapon = weapon,
                            isSelected = isSelected,
                            onSelect = { onSelectWeapon(index) }
                        )
                    } else if (weapon.type.isModernAdvanced) {
                        // Locked or disabled modern weapon with AdMob unlock trigger
                        LockedModernWeaponTab(
                            weapon = weapon,
                            onUnlockWithAdMob = { onUnlockWithAdMob(weapon.type) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Quick Instant Reload with Score (80 PTS)
            val currentWeapon = uiState.weapons.getOrNull(uiState.selectedWeaponIndex)
            val canReloadWithScore = uiState.score >= 80 && currentWeapon != null && currentWeapon.currentAmmo < currentWeapon.maxAmmo
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (canReloadWithScore) Color(0xFF1E2630) else Color(0xFF161B22))
                    .border(
                        1.dp,
                        if (canReloadWithScore) Color(0xFF00E676) else Color(0x338B949E),
                        RoundedCornerShape(12.dp)
                    )
                    .clickable(enabled = canReloadWithScore) { onInstantReloadWithScore() }
                    .padding(horizontal = 8.dp, vertical = 8.dp)
                    .testTag("score_reload_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.ElectricBolt,
                        contentDescription = "Recarga con Puntos",
                        tint = if (canReloadWithScore) Color(0xFF00E676) else Color(0xFF484F58),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "RÁPIDA",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = if (canReloadWithScore) Color(0xFF00E676) else Color(0xFF8B949E)
                        )
                        Text(
                            text = "80 PTS",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (canReloadWithScore) Color(0xFFFFD54F) else Color(0xFF484F58)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Standard Manual Reload Button
            val isReloading = currentWeapon?.isReloading == true
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isReloading) Color(0xFFFF9100) else Color(0xFF21262D))
                    .border(
                        1.dp,
                        if (isReloading) Color(0xFFFFD54F) else Color(0xFF30363D),
                        RoundedCornerShape(12.dp)
                    )
                    .clickable(enabled = !isReloading) { onReload() }
                    .padding(horizontal = 10.dp, vertical = 10.dp)
                    .testTag("reload_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Recargar",
                        tint = if (isReloading) Color.Black else Color(0xFF00E676),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = if (isReloading) "%.0f%%".format((currentWeapon?.reloadProgress ?: 0f) * 100) else "RECARGAR",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isReloading) Color.Black else Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun LockedModernWeaponTab(
    weapon: WeaponState,
    onUnlockWithAdMob: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xEE161B22))
            .border(1.dp, Color(0xFF4285F4), RoundedCornerShape(12.dp))
            .clickable { onUnlockWithAdMob() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = when (weapon.type) {
                        WeaponType.SNIPER -> "🎯"
                        WeaponType.GRENADE_LAUNCHER -> "💣"
                        WeaponType.PLASMA_CANNON -> "⚛️"
                        else -> "⭐"
                    },
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = weapon.type.displayName.split(" ").firstOrNull() ?: weapon.type.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color(0xFF90CAF9)
                    )
                    Text(
                        text = "MODERNA",
                        fontSize = 8.sp,
                        color = Color(0xFF4285F4),
                        fontWeight = FontWeight.Black
                    )
                }
            }
            Spacer(modifier = Modifier.height(3.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF4285F4))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "📺 AdMob",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun WeaponItemTab(
    weapon: WeaponState,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val borderColor = if (isSelected) Color(0xFF00E676) else Color(0xFF30363D)
    val bgColor = if (isSelected) Color(0xEE1E2630) else Color(0xCC161B22)

    val caliberText = when (weapon.type) {
        WeaponType.PISTOL -> "9mm NATO"
        WeaponType.SHOTGUN -> "12 GAUGE"
        WeaponType.ASSAULT_RIFLE -> "5.56x45"
        WeaponType.SNIPER -> ".50 BMG"
        WeaponType.GRENADE_LAUNCHER -> "84mm HE"
        WeaponType.PLASMA_CANNON -> "ION CELL"
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onSelect() }
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = when (weapon.type) {
                        WeaponType.PISTOL -> "🔫"
                        WeaponType.SHOTGUN -> "💥"
                        WeaponType.ASSAULT_RIFLE -> "⚡"
                        WeaponType.SNIPER -> "🎯"
                        WeaponType.GRENADE_LAUNCHER -> "💣"
                        WeaponType.PLASMA_CANNON -> "⚛️"
                    },
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = weapon.type.displayName.split(" ").firstOrNull() ?: weapon.type.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (isSelected) Color(0xFF00E676) else Color.White
                    )
                    Text(
                        text = caliberText,
                        fontSize = 9.sp,
                        color = Color(0xFF8B949E),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            val ammoRatio = (weapon.currentAmmo.toFloat() / weapon.maxAmmo.coerceAtLeast(1)).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = { ammoRatio },
                modifier = Modifier
                    .width(70.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(1.5.dp)),
                color = when {
                    weapon.isReloading -> Color(0xFFFF9100)
                    ammoRatio > 0.4f -> Color(0xFF00E676)
                    else -> Color(0xFFFF1744)
                },
                trackColor = Color(0xFF30363D)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (weapon.isReloading) "Recargando..." else "${weapon.currentAmmo}/${weapon.maxAmmo}",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (weapon.currentAmmo == 0) Color(0xFFFF1744) else Color(0xFFE6EDF3)
            )
        }
    }
}

@Composable
private fun SupportQuickButton(
    item: SupportItem,
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isAvailable = count > 0

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isAvailable) Color(0xFF21262D) else Color(0xFF161B22))
            .border(
                1.dp,
                if (isAvailable) Color(0xFF30363D) else Color(0x228B949E),
                RoundedCornerShape(10.dp)
            )
            .clickable(enabled = isAvailable) { onClick() }
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(item.iconEmoji, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "x$count",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isAvailable) Color.White else Color(0xFF484F58)
            )
        }
    }
}
