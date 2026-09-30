package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Upgrade
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.viewmodel.GameUiState

@Composable
fun PointsUpgradeDialog(
    uiState: GameUiState,
    onUpgradeDamage: () -> Unit,
    onUpgradeBarricade: () -> Unit,
    onRepairBarricade: () -> Unit,
    onUpgradeSpikes: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(
                        width = 1.5.dp,
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFFFFD54F), Color(0xFF00E5FF), Color(0xFF1F2937))
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .testTag("points_upgrade_dialog"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111722)),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x33FFD54F)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD54F),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "CANJE DE PUNTOS",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Text(
                                    text = "Mejoras tácticas de combate",
                                    fontSize = 11.sp,
                                    color = Color(0xFF9CA3AF)
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("close_points_upgrade")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = Color(0xFF9CA3AF)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Points Balance Pill
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF1E2638), Color(0xFF283548))
                                )
                            )
                            .border(1.dp, Color(0x55FFD54F), RoundedCornerShape(14.dp))
                            .padding(vertical = 10.dp, horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "PUNTOS DISPONIBLES: ",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFE5E7EB)
                            )
                            Text(
                                text = "${uiState.score} ⭐",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFFD54F)
                            )
                        }
                    }

                    // Optional Feedback Banner
                    AnimatedVisibility(
                        visible = uiState.feedbackMessage != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        uiState.feedbackMessage?.let { msg ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0x3300E676))
                                    .border(1.dp, Color(0xFF00E676), RoundedCornerShape(10.dp))
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = msg,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00E676),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Upgrade 1: Daño Balístico Global
                    UpgradeItemCard(
                        title = "POTENCIA DE FUEGO",
                        subtitle = "+15% daño en todas las armas por nivel",
                        currentBonus = "Daño actual: +${((uiState.pointsDamageLevel - 1) * 15)}%",
                        level = uiState.pointsDamageLevel,
                        maxLevel = 10,
                        cost = uiState.nextDamageUpgradeCost,
                        canAfford = uiState.score >= uiState.nextDamageUpgradeCost && uiState.pointsDamageLevel < 10,
                        icon = Icons.Default.LocalFireDepartment,
                        accentColor = Color(0xFFFF7043),
                        onUpgrade = onUpgradeDamage,
                        testTag = "upgrade_damage_btn"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Upgrade 2: Blindaje y Resistencia de Barricada
                    UpgradeItemCard(
                        title = "BLINDAJE DE BARRICADA",
                        subtitle = "+30 HP Máx y +8% absorción de impacto zombi",
                        currentBonus = "Resistencia: ${((uiState.pointsBarricadeLevel - 1) * 8)}% | Salud: ${uiState.barricadeMaxHp.toInt()} HP",
                        level = uiState.pointsBarricadeLevel,
                        maxLevel = 10,
                        cost = uiState.nextBarricadeUpgradeCost,
                        canAfford = uiState.score >= uiState.nextBarricadeUpgradeCost && uiState.pointsBarricadeLevel < 10,
                        icon = Icons.Default.Shield,
                        accentColor = Color(0xFF00E676),
                        onUpgrade = onUpgradeBarricade,
                        testTag = "upgrade_barricade_btn"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Upgrade 3: Reparación Nanocelular Inmediata
                    val needsRepair = uiState.barricadeCurrentHp < uiState.barricadeMaxHp
                    ActionItemCard(
                        title = "REPARACIÓN INMEDIATA",
                        subtitle = "Restaura +45 HP a la barricada al instante",
                        statusText = "Salud: ${uiState.barricadeCurrentHp.toInt()} / ${uiState.barricadeMaxHp.toInt()} HP",
                        cost = uiState.repairPointsCost,
                        isEnabled = uiState.score >= uiState.repairPointsCost && needsRepair,
                        icon = Icons.Default.Upgrade,
                        accentColor = Color(0xFF64B5F6),
                        buttonText = if (!needsRepair) "AL MÁXIMO" else "REPARAR (+45 HP)",
                        onAction = onRepairBarricade,
                        testTag = "repair_barricade_btn"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Upgrade 4: Púas de Represalia Electrificadas
                    UpgradeItemCard(
                        title = "PÚAS ELECTRIFICADAS",
                        subtitle = "Descarga de alto voltaje al ser atacada la barricada",
                        currentBonus = if (uiState.barricadeSpikesLevel > 0) "Descarga: ${uiState.barricadeSpikesLevel * 18} Daño eléctrico" else "Sin instalar",
                        level = uiState.barricadeSpikesLevel,
                        maxLevel = 5,
                        cost = uiState.nextSpikesCost,
                        canAfford = uiState.score >= uiState.nextSpikesCost && uiState.barricadeSpikesLevel < 5,
                        icon = Icons.Default.ElectricBolt,
                        accentColor = Color(0xFF00E5FF),
                        onUpgrade = onUpgradeSpikes,
                        testTag = "upgrade_spikes_btn"
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Close Button
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("dismiss_points_dialog_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF263238)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("VOLVER AL COMBATE", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun UpgradeItemCard(
    title: String,
    subtitle: String,
    currentBonus: String,
    level: Int,
    maxLevel: Int,
    cost: Int,
    canAfford: Boolean,
    icon: ImageVector,
    accentColor: Color,
    onUpgrade: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF18202F)),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2D3748))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(accentColor.copy(alpha = 0.25f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (level >= maxLevel) "MÁX" else "NV. $level/$maxLevel",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = accentColor
                            )
                        }
                    }
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { (level.toFloat() / maxLevel).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = accentColor,
                trackColor = Color(0xFF1E293B)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currentBonus,
                    fontSize = 10.sp,
                    color = Color(0xFFCBD5E1)
                )

                if (level < maxLevel) {
                    Button(
                        onClick = onUpgrade,
                        enabled = canAfford,
                        modifier = Modifier
                            .height(34.dp)
                            .testTag(testTag),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentColor,
                            disabledContainerColor = Color(0xFF334155)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "MEJORAR ($cost ⭐)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = if (canAfford) Color.Black else Color(0xFF94A3B8)
                        )
                    }
                } else {
                    Text(
                        text = "TOTALMENTE MEJORADO ✓",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E676)
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionItemCard(
    title: String,
    subtitle: String,
    statusText: String,
    cost: Int,
    isEnabled: Boolean,
    icon: ImageVector,
    accentColor: Color,
    buttonText: String,
    onAction: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF18202F)),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2D3748))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = statusText,
                    fontSize = 10.sp,
                    color = Color(0xFFCBD5E1)
                )

                Button(
                    onClick = onAction,
                    enabled = isEnabled,
                    modifier = Modifier
                        .height(34.dp)
                        .testTag(testTag),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor,
                        disabledContainerColor = Color(0xFF334155)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isEnabled) "$buttonText ($cost ⭐)" else buttonText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isEnabled) Color.Black else Color(0xFF94A3B8)
                    )
                }
            }
        }
    }
}
