package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Upgrade
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.SupportItem
import com.example.model.WeaponState
import com.example.model.WeaponType
import com.example.viewmodel.GameUiState

@Composable
fun ShopDialog(
    uiState: GameUiState,
    onDismiss: () -> Unit,
    onBuyWeapon: (WeaponType) -> Unit,
    onUpgradeWeapon: (WeaponType) -> Unit,
    onUpgradeBarricade: () -> Unit,
    onBuySupportItem: (SupportItem) -> Unit,
    onWatchAdToUnlock: (WeaponType) -> Unit = {},
    onToggleModernWeapons: () -> Unit = {},
    onExchangeScoreForCoins: () -> Unit = {},
    onRechargeScoreWithAdMob: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xE60D1117))
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
                    .align(Alignment.Center)
                    .border(1.dp, Color(0xFF30363D), RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp)
                ) {
                    // Header: Title & Coin Balance & Close button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ARMERÍA Y SUMINISTROS",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                ),
                                color = Color(0xFF00E676)
                            )
                            Text(
                                text = "Puntuación: ${uiState.score} PTS | Monedas: ${uiState.coins} 🪙",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFFFD54F)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF21262D))
                                    .border(1.dp, Color(0xFFFFD54F), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${uiState.coins} 🪙",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD54F),
                                    fontSize = 15.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.testTag("shop_close_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cerrar tienda",
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Tab Selector
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color(0xFF21262D),
                        contentColor = Color(0xFF00E676),
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = Color(0xFF00E676)
                            )
                        },
                        modifier = Modifier.clip(RoundedCornerShape(12.dp))
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = {
                                Text(
                                    "ARMAS (${uiState.weapons.count { it.isUnlocked }}/${uiState.weapons.size})",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = {
                                Text(
                                    "BARRICADA Y APOYO",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Content Tabs
                    Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        if (selectedTab == 0) {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                            // Section 1: Modern Weapons Toggle & AdMob Recharge Control
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF21262D)),
                                    shape = RoundedCornerShape(14.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4285F4))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(Color(0xFF4285F4))
                                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                                ) {
                                                    Text("Google AdMob", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.White)
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Bonificaciones Oficiales",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }

                                            // Toggle Modern Weapons Button
                                            Button(
                                                onClick = onToggleModernWeapons,
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (uiState.areModernWeaponsDisabled) Color(0xFFBF360C) else Color(0xFF238636),
                                                    contentColor = Color.White
                                                ),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (uiState.areModernWeaponsDisabled) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (uiState.areModernWeaponsDisabled) "HABILITAR MODERNAS" else "DESHABILITAR MODERNAS",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // 2 Action Buttons: Exchange Score for Coins & Recharge with AdMob
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            // Exchange score for coins
                                            OutlinedButton(
                                                onClick = onExchangeScoreForCoins,
                                                enabled = uiState.score >= 300,
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFD54F))
                                            ) {
                                                Icon(Icons.Default.ElectricBolt, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Canjear 300 Pts (+100 🪙)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }

                                            // Recharge with AdMob
                                            Button(
                                                onClick = onRechargeScoreWithAdMob,
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676), contentColor = Color.Black)
                                            ) {
                                                Icon(Icons.Default.PlayCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("+1500 PTS / +300 🪙", fontSize = 11.sp, fontWeight = FontWeight.Black)
                                            }
                                        }
                                    }
                                }
                            }

                            items(uiState.weapons) { weapon ->
                                WeaponShopCard(
                                    weapon = weapon,
                                    userCoins = uiState.coins,
                                    currentWave = uiState.wave,
                                    areModernDisabled = uiState.areModernWeaponsDisabled,
                                    onBuy = { onBuyWeapon(weapon.type) },
                                    onUpgrade = { onUpgradeWeapon(weapon.type) },
                                    onWatchAdToUnlock = { onWatchAdToUnlock(weapon.type) }
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Barricade Defense Card
                            item {
                                BarricadeUpgradeCard(
                                    uiState = uiState,
                                    onUpgrade = onUpgradeBarricade
                                )
                            }

                            // Support items
                            item {
                                Text(
                                    text = "SUMINISTROS TÁCTICOS DE CAMPO",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFE6EDF3),
                                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                )
                            }

                            items(SupportItem.entries) { item ->
                                SupportItemShopCard(
                                    item = item,
                                    quantity = uiState.supportItems[item] ?: 0,
                                    userCoins = uiState.coins,
                                    onBuy = { onBuySupportItem(item) }
                                )
                            }
                        }
                    }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Google AdMob VIP Banner
                    AdMobBanner(
                        adUnitId = com.example.ads.AdMobConstants.BANNER_VIP,
                        isVip = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun WeaponShopCard(
    weapon: WeaponState,
    userCoins: Int,
    currentWave: Int,
    areModernDisabled: Boolean = false,
    onBuy: () -> Unit,
    onUpgrade: () -> Unit,
    onWatchAdToUnlock: () -> Unit = {}
) {
    val canAffordBuy = userCoins >= weapon.type.basePrice
    val canAffordUpgrade = userCoins >= weapon.nextUpgradeCost
    val isMaxLevel = weapon.upgradeLevel >= 5
    val isLockedByWave = currentWave < weapon.type.unlockWave
    val isModern = weapon.type.isModernAdvanced

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (weapon.isUnlocked && (!isModern || !areModernDisabled)) Color(0xFF21262D) else Color(0xFF161B22)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isModern) Color(0xFF4285F4)
            else if (weapon.isUnlocked) Color(0xFF30363D) else Color(0x33FF1744)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                if (weapon.isUnlocked) Color(0xFF00E676).copy(alpha = 0.15f)
                                else Color(0xFF30363D)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (weapon.type) {
                                WeaponType.PISTOL -> "🔫"
                                WeaponType.SHOTGUN -> "💥"
                                WeaponType.ASSAULT_RIFLE -> "⚡"
                                WeaponType.SNIPER -> "🎯"
                                WeaponType.GRENADE_LAUNCHER -> "💣"
                                WeaponType.PLASMA_CANNON -> "⚛️"
                            },
                            fontSize = 20.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = weapon.type.displayName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (weapon.isUnlocked) Color.White else Color(0xFF8B949E)
                            )
                            if (isModern) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF1E88E5))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text("MODERNA", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.White)
                                }
                            }
                        }
                        // Star rating for upgrade level
                        Row {
                            for (i in 1..5) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (i <= weapon.upgradeLevel && weapon.isUnlocked) Color(0xFFFFD54F) else Color(0xFF484F58),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            if (weapon.isUnlocked) {
                                Text(
                                    text = " Tier ${weapon.upgradeLevel}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF8B949E),
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Action Button (Buy or Upgrade or AdMob unlock)
                if (!weapon.isUnlocked || (isModern && areModernDisabled)) {
                    Column(horizontalAlignment = Alignment.End) {
                        if (isModern) {
                            Button(
                                onClick = onWatchAdToUnlock,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4285F4),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("📺 AdMob Desbloquear", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        if (!weapon.isUnlocked && !areModernDisabled) {
                            if (isLockedByWave) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF30363D))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = Color(0xFFFF9100),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            "Ola ${weapon.type.unlockWave}",
                                            fontSize = 11.sp,
                                            color = Color(0xFFFF9100),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            } else {
                                Button(
                                    onClick = onBuy,
                                    enabled = canAffordBuy,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF00E676),
                                        contentColor = Color.Black
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text("${weapon.type.basePrice} 🪙", fontWeight = FontWeight.Black, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                } else {
                    if (isMaxLevel) {
                        Text(
                            text = "MAX Nivel",
                            color = Color(0xFF00E676),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    } else {
                        Button(
                            onClick = onUpgrade,
                            enabled = canAffordUpgrade,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF238636),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Upgrade, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${weapon.nextUpgradeCost} 🪙", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Weapon description
            Text(
                text = weapon.type.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF8B949E)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Weapon stats indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatMini(label = "Daño", value = "${weapon.damage.toInt()}")
                StatMini(label = "Cargador", value = "${weapon.maxAmmo}")
                StatMini(label = "Cadencia", value = "${1000 / weapon.fireRateMs}/s")
                StatMini(label = "Recarga", value = "%.1fs".format(weapon.reloadTimeMs / 1000f))
            }
        }
    }
}

@Composable
private fun StatMini(label: String, value: String) {
    Column {
        Text(text = label, fontSize = 10.sp, color = Color(0xFF8B949E))
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE6EDF3))
    }
}

@Composable
private fun BarricadeUpgradeCard(
    uiState: GameUiState,
    onUpgrade: () -> Unit
) {
    val upgradeCost = 90 + ((uiState.barricadeMaxHp - 100f) / 35f * 60).toInt()
    val canAfford = uiState.coins >= upgradeCost

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF21262D)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF30363D))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E88E5).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFF42A5F5))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Muro de Defensa Fortificado",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "Salud: ${uiState.barricadeCurrentHp.toInt()}/${uiState.barricadeMaxHp.toInt()} HP | Púas: Lvl ${uiState.barricadeSpikesLevel}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF8B949E)
                        )
                    }
                }

                Button(
                    onClick = onUpgrade,
                    enabled = canAfford,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E88E5),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("$upgradeCost 🪙", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Aumenta la salud máxima de la barricada en +35 HP e incrementa el contraataque de púas que daña a los zombis que golpean tu muro.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF8B949E)
            )
        }
    }
}

@Composable
private fun SupportItemShopCard(
    item: SupportItem,
    quantity: Int,
    userCoins: Int,
    onBuy: () -> Unit
) {
    val canAfford = userCoins >= item.cost

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF21262D)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF30363D))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(item.iconEmoji, fontSize = 28.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.displayName,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF30363D))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Tienes: $quantity",
                                fontSize = 11.sp,
                                color = Color(0xFF00E676),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF8B949E),
                        maxLines = 2
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onBuy,
                enabled = canAfford,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF238636),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("${item.cost} 🪙", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}
