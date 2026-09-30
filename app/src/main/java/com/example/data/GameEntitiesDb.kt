package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_records")
data class GameRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val waveReached: Int,
    val score: Int,
    val kills: Int,
    val durationSeconds: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "player_profile")
data class PlayerProfileEntity(
    @PrimaryKey
    val id: Int = 1,
    val totalCoins: Int = 50,
    val highestWave: Int = 1,
    val highestScore: Int = 0,
    val lifetimeKills: Int = 0,
    val barricadeLevel: Int = 1, // 1 to 5
    val unlockedWeapons: String = "pistol", // comma-separated weapon IDs
    val weaponLevels: String = "pistol:1" // format: pistol:1,shotgun:1
)
