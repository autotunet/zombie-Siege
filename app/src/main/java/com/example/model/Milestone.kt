package com.example.model

enum class MilestoneId(
    val title: String,
    val description: String,
    val iconEmoji: String,
    val rewardPoints: Int,
    val rewardCoins: Int,
    val targetGoal: Int = 1,
    val category: String = "COMBATE"
) {
    HEADSHOT_SNIPER(
        title = "Disparo a la cabeza",
        description = "Asesta un impacto crítico mortal en la cabeza de un zombi.",
        iconEmoji = "🎯",
        rewardPoints = 150,
        rewardCoins = 15,
        targetGoal = 1,
        category = "PRECISIÓN"
    ),
    FIRST_BLOOD(
        title = "Primer Contacto",
        description = "Elimina a tu primer invasor zombi.",
        iconEmoji = "🩸",
        rewardPoints = 50,
        rewardCoins = 10,
        targetGoal = 1,
        category = "SUPERVIVENCIA"
    ),
    KILLS_25(
        title = "Cazador Callejero",
        description = "Elimina 25 zombis durante tus patrullas urbanas.",
        iconEmoji = "🗡️",
        rewardPoints = 150,
        rewardCoins = 25,
        targetGoal = 25,
        category = "BAJAS"
    ),
    KILLS_100(
        title = "100 zombis eliminados",
        description = "Aniquila una centena completa de infectados de la plaga.",
        iconEmoji = "💀",
        rewardPoints = 400,
        rewardCoins = 60,
        targetGoal = 100,
        category = "BAJAS"
    ),
    KILLS_250(
        title = "Pesadilla de la Horda (250)",
        description = "Alcanza la colosal cifra de 250 bajas confirmadas.",
        iconEmoji = "🔥",
        rewardPoints = 800,
        rewardCoins = 120,
        targetGoal = 250,
        category = "BAJAS"
    ),
    WAVE_5(
        title = "Resistencia Urbana",
        description = "Sobrevive a las primeras embestidas y alcanza la Oleada 5.",
        iconEmoji = "🛡️",
        rewardPoints = 250,
        rewardCoins = 35,
        targetGoal = 5,
        category = "OLEADAS"
    ),
    WAVE_10(
        title = "Oleada 10 alcanzada",
        description = "Demuestra tu supremacía táctica sobreviviendo hasta la Oleada 10.",
        iconEmoji = "🏆",
        rewardPoints = 600,
        rewardCoins = 100,
        targetGoal = 10,
        category = "OLEADAS"
    ),
    BOSS_SLAYER(
        title = "Cazador de Titanes",
        description = "Derrota a un temible Jefe Mutante Gigante en combate.",
        iconEmoji = "🦖",
        rewardPoints = 500,
        rewardCoins = 80,
        targetGoal = 1,
        category = "JEFES"
    ),
    COMBO_STREAK_5(
        title = "Frenesí de Balas (Combo x5)",
        description = "Encadena una racha ininterrumpida de 5 bajas consecutivas.",
        iconEmoji = "⚡",
        rewardPoints = 200,
        rewardCoins = 30,
        targetGoal = 5,
        category = "COMBATE"
    ),
    BARRICADE_UPGRADE(
        title = "Fortaleza Reforzada",
        description = "Canjea puntos o créditos para mejorar la resistencia de tu barricada.",
        iconEmoji = "🧱",
        rewardPoints = 150,
        rewardCoins = 20,
        targetGoal = 1,
        category = "DEFENSA"
    )
}

data class ActiveMilestoneNotification(
    val milestone: MilestoneId,
    val timestamp: Long = System.currentTimeMillis()
)
