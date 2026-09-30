package com.example.data

import kotlinx.coroutines.flow.Flow

class GameRepository(private val gameDao: GameDao) {

    val recentRecords: Flow<List<GameRecordEntity>> = gameDao.getRecentRecords()
    val playerProfile: Flow<PlayerProfileEntity?> = gameDao.getPlayerProfile()

    suspend fun getProfileDirect(): PlayerProfileEntity {
        val existing = gameDao.getPlayerProfileDirect()
        if (existing != null) {
            return existing
        }
        val defaultProfile = PlayerProfileEntity()
        gameDao.insertOrUpdateProfile(defaultProfile)
        return defaultProfile
    }

    suspend fun saveGameRun(wave: Int, score: Int, kills: Int, durationSeconds: Int) {
        val record = GameRecordEntity(
            waveReached = wave,
            score = score,
            kills = kills,
            durationSeconds = durationSeconds
        )
        gameDao.insertRecord(record)

        val profile = getProfileDirect()
        val newHighestWave = maxOf(profile.highestWave, wave)
        val newHighestScore = maxOf(profile.highestScore, score)
        val newLifetimeKills = profile.lifetimeKills + kills

        gameDao.insertOrUpdateProfile(
            profile.copy(
                highestWave = newHighestWave,
                highestScore = newHighestScore,
                lifetimeKills = newLifetimeKills
            )
        )
    }

    suspend fun updateCoins(delta: Int) {
        val profile = getProfileDirect()
        val updatedCoins = (profile.totalCoins + delta).coerceAtLeast(0)
        gameDao.insertOrUpdateProfile(profile.copy(totalCoins = updatedCoins))
    }

    suspend fun updateProfile(profile: PlayerProfileEntity) {
        gameDao.insertOrUpdateProfile(profile)
    }
}
