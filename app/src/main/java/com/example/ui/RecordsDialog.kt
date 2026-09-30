package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import com.example.data.GameRecordEntity
import com.example.data.PlayerProfileEntity
import com.example.model.MilestoneId
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.min

@Composable
fun RecordsDialog(
    profile: PlayerProfileEntity?,
    records: List<GameRecordEntity>,
    onDismiss: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    var selectedTab by remember { mutableIntStateOf(0) }

    val unlockedSet = remember(profile?.unlockedMilestones) {
        profile?.unlockedMilestones?.split(",")?.filter { it.isNotBlank() }?.toSet() ?: emptySet()
    }

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
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFD54F).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFFFD54F))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "HISTORIAL Y LOGROS",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                                color = Color.White
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("records_close_button")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Lifetime Stats Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCard(
                            label = "Mejor Ola",
                            value = "${profile?.highestWave ?: 1}",
                            color = Color(0xFFFF9100),
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            label = "Récord Puntos",
                            value = "${profile?.highestScore ?: 0}",
                            color = Color(0xFF00E676),
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            label = "Bajas Totales",
                            value = "${profile?.lifetimeKills ?: 0}",
                            color = Color(0xFF40C4FF),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Tab Selector: Historial vs Hitos
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color(0xFF0D1117),
                        contentColor = Color(0xFFFFD54F),
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = Color(0xFFFFD54F),
                                height = 3.dp
                            )
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, Color(0xFF30363D), RoundedCornerShape(10.dp))
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = {
                                Text(
                                    "PARTIDAS RECIENTES",
                                    fontWeight = if (selectedTab == 0) FontWeight.Black else FontWeight.Normal,
                                    fontSize = 11.sp
                                )
                            },
                            modifier = Modifier.testTag("tab_recent_records")
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = {
                                Text(
                                    "HITOS (${unlockedSet.size}/${MilestoneId.entries.size})",
                                    fontWeight = if (selectedTab == 1) FontWeight.Black else FontWeight.Normal,
                                    fontSize = 11.sp
                                )
                            },
                            modifier = Modifier.testTag("tab_milestones")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (selectedTab == 0) {
                        // Recents View
                        if (records.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Aún no hay partidas registradas.\n¡Inicia tu primera batalla!",
                                    color = Color(0xFF8B949E),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                items(records) { record ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF21262D)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.MilitaryTech,
                                                    contentDescription = null,
                                                    tint = Color(0xFFFF9100),
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text(
                                                        text = "Oleada ${record.waveReached}",
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                    Text(
                                                        text = dateFormat.format(Date(record.timestamp)),
                                                        fontSize = 11.sp,
                                                        color = Color(0xFF8B949E)
                                                    )
                                                }
                                            }

                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    text = "${record.score} pts",
                                                    fontWeight = FontWeight.Black,
                                                    color = Color(0xFF00E676)
                                                )
                                                Text(
                                                    text = "${record.kills} bajas • %02d:%02d".format(
                                                        record.durationSeconds / 60,
                                                        record.durationSeconds % 60
                                                    ),
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF8B949E)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Milestones List View
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(MilestoneId.entries) { milestone ->
                                val isUnlocked = unlockedSet.contains(milestone.name)
                                MilestoneCard(
                                    milestone = milestone,
                                    isUnlocked = isUnlocked,
                                    lifetimeKills = profile?.lifetimeKills ?: 0,
                                    highestWave = profile?.highestWave ?: 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MilestoneCard(
    milestone: MilestoneId,
    isUnlocked: Boolean,
    lifetimeKills: Int,
    highestWave: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (isUnlocked) Color(0xFFFFD54F) else Color(0xFF263238),
                shape = RoundedCornerShape(14.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) Color(0xFF1E2838) else Color(0xFF131A22)
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        if (isUnlocked) Color(0x33FFD54F) else Color(0xFF1F2937)
                    )
                    .border(
                        1.dp,
                        if (isUnlocked) Color(0xFFFFD54F) else Color(0xFF374151),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = milestone.iconEmoji,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Info & Description
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = milestone.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (isUnlocked) Color.White else Color(0xFF94A3B8)
                    )

                    if (isUnlocked) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF00E676),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "LOGRADO",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF00E676)
                            )
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "BLOQUEADO",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }

                Text(
                    text = milestone.description,
                    fontSize = 11.sp,
                    color = Color(0xFF8B949E)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Progress Indicator (if locked)
                if (!isUnlocked && milestone.targetGoal > 1) {
                    val currentProgress = when (milestone) {
                        MilestoneId.KILLS_25,
                        MilestoneId.KILLS_100,
                        MilestoneId.KILLS_250 -> min(lifetimeKills, milestone.targetGoal)
                        MilestoneId.WAVE_5,
                        MilestoneId.WAVE_10 -> min(highestWave, milestone.targetGoal)
                        else -> 0
                    }
                    val ratio = (currentProgress.toFloat() / milestone.targetGoal).coerceIn(0f, 1f)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LinearProgressIndicator(
                            progress = { ratio },
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = Color(0xFFFFD54F),
                            trackColor = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$currentProgress/${milestone.targetGoal}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                // Reward Tag
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Recompensa: +${milestone.rewardPoints} ⭐  +${milestone.rewardCoins} 🪙",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFFD54F)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF21262D))
            .border(1.dp, Color(0xFF30363D), RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(text = label, fontSize = 11.sp, color = Color(0xFF8B949E))
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Black, color = color)
        }
    }
}
