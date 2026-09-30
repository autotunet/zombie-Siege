package com.example.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.WeaponType

@Composable
fun AdMobRewardDialog(
    countdownSeconds: Int,
    rewardType: String,
    rewardWeapon: WeaponType?,
    onClaimReward: () -> Unit,
    onDismiss: () -> Unit
) {
    val progress by animateFloatAsState(
        targetValue = (5 - countdownSeconds) / 5f,
        label = "admob_progress"
    )

    val isVipReward = rewardType == "UNLOCK_WEAPON" || rewardType.contains("RECHARGE") || rewardType.contains("PRESTAME")
    val adUnitId = if (isVipReward) {
        com.example.ads.AdMobConstants.REWARDED_PRESTAME_VIP
    } else {
        com.example.ads.AdMobConstants.REWARDED_SECONDARY
    }
    val adTitle = if (isVipReward) "AdMob Rewarded / Prestamevip" else "AdMob Video Bonificado"

    Dialog(
        onDismissRequest = {
            if (countdownSeconds == 0) onDismiss()
        },
        properties = DialogProperties(
            dismissOnBackPress = countdownSeconds == 0,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xF0000000))
                .padding(20.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .align(Alignment.Center)
                    .border(1.dp, Color(0xFF4285F4), RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131720))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Google AdMob Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isVipReward) Color(0xFFFFB300) else Color(0xFF4285F4))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (isVipReward) "AdMob VIP" else "Google AdMob",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isVipReward) Color.Black else Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = adTitle,
                                    fontSize = 11.sp,
                                    color = if (isVipReward) Color(0xFFFFD54F) else Color(0xFF90CAF9),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = adUnitId,
                                    fontSize = 8.sp,
                                    color = Color(0xFF8B949E),
                                    maxLines = 1
                                )
                            }
                        }

                        if (countdownSeconds == 0) {
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.size(28.dp).testTag("admob_close_button")
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color(0xFF263238))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${countdownSeconds}s",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD54F)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Simulated Video Ad Player Container
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF1A237E), Color(0xFF0D47A1), Color(0xFF01579B))
                                )
                            )
                            .border(1.dp, Color(0xFF29B6F6), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.PlayCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "TACTICAL DEFENSE PROTOCOL",
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Reproduciendo anuncio oficial de prueba...",
                                fontSize = 11.sp,
                                color = Color(0xFFB3E5FC)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Progress Bar
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFF00E676),
                        trackColor = Color(0xFF263238)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Reward Description Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B222C)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00E676).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = Color(0xFF00E676))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (countdownSeconds > 0) "Recompensa en curso..." else "¡RECOMPENSA OBTENIDA!",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (countdownSeconds > 0) Color(0xFFFFD54F) else Color(0xFF00E676)
                                )
                                Text(
                                    text = when (rewardType) {
                                        "UNLOCK_WEAPON" -> "Desbloquear ${rewardWeapon?.displayName ?: "Arma"} + 200 🪙 + 1,000 PTS"
                                        "REFILL_AMMO" -> "Recarga 100% de Munición + 200 🪙 + 800 PTS"
                                        "RECHARGE_SCORE", "RECHARGE_SCORE_AND_COINS" -> "+1,500 Puntos + 300 🪙 + Munición 100%"
                                        else -> "250 Monedas + 1,000 Puntos Gratis"
                                    },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Action Button
                    Button(
                        onClick = onClaimReward,
                        enabled = countdownSeconds == 0,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("admob_claim_reward_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00E676),
                            contentColor = Color.Black,
                            disabledContainerColor = Color(0xFF263238),
                            disabledContentColor = Color(0xFF8B949E)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (countdownSeconds > 0) "ESPERA ${countdownSeconds}s PARA RECLAMAR" else "RECLAMAR RECOMPENSA AHORA",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
