package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.sin
import kotlin.random.Random

class GameAudio(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Default)

    var soundEnabled: Boolean = true
    var hapticsEnabled: Boolean = true

    private val vibrator: Vibrator? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    } catch (_: Exception) {
        null
    }

    private val sampleRate = 22050
    private val audioTracksCache = ConcurrentHashMap<String, ByteArray>()

    init {
        // Pre-generate short audio byte buffers for fast playback
        scope.launch {
            try {
                audioTracksCache["pistol"] = generatePistolPcm()
                audioTracksCache["shotgun"] = generateShotgunPcm()
                audioTracksCache["assault"] = generateRiflePcm()
                audioTracksCache["sniper"] = generateSniperPcm()
                audioTracksCache["explosion"] = generateExplosionPcm()
                audioTracksCache["plasma"] = generatePlasmaPcm()
                audioTracksCache["reload"] = generateReloadPcm()
                audioTracksCache["hit"] = generateHitPcm()
                audioTracksCache["wave"] = generateWaveFanfarePcm()
                audioTracksCache["gameover"] = generateGameOverPcm()
            } catch (_: Exception) {
            }
        }
    }

    private fun playPcm(buffer: ByteArray) {
        if (!soundEnabled) return
        scope.launch {
            try {
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_8BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(buffer, 0, buffer.size)
                track.play()
                track.setNotificationMarkerPosition(buffer.size)
                track.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
                    override fun onMarkerReached(t: AudioTrack?) {
                        t?.release()
                    }
                    override fun onPeriodicNotification(t: AudioTrack?) {}
                })
            } catch (_: Exception) {
            }
        }
    }

    fun playGunshot(type: String) {
        val soundKey = when (type.lowercase()) {
            "shotgun" -> "shotgun"
            "assault_rifle" -> "assault"
            "sniper" -> "sniper"
            "rpg", "grenade_launcher" -> "explosion"
            "plasma" -> "plasma"
            else -> "pistol"
        }
        val pcm = audioTracksCache[soundKey]
        if (pcm != null) {
            playPcm(pcm)
        }
        vibrateLight()
    }

    fun playExplosion() {
        audioTracksCache["explosion"]?.let { playPcm(it) }
        vibrateHeavy()
    }

    fun playZombieHit() {
        audioTracksCache["hit"]?.let { playPcm(it) }
    }

    fun playReload() {
        audioTracksCache["reload"]?.let { playPcm(it) }
    }

    fun playWaveClear() {
        audioTracksCache["wave"]?.let { playPcm(it) }
    }

    fun playUpgrade() {
        audioTracksCache["wave"]?.let { playPcm(it) }
        vibrateLight()
    }

    fun playRepair() {
        audioTracksCache["reload"]?.let { playPcm(it) }
        vibrateLight()
    }

    fun playGameOver() {
        audioTracksCache["gameover"]?.let { playPcm(it) }
        vibrateHeavy()
    }

    fun vibrateLight() {
        if (!hapticsEnabled || vibrator == null || !vibrator.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(18, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(18)
            }
        } catch (_: Exception) {}
    }

    fun vibrateMedium() {
        if (!hapticsEnabled || vibrator == null || !vibrator.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(45)
            }
        } catch (_: Exception) {}
    }

    fun vibrateHeavy() {
        if (!hapticsEnabled || vibrator == null || !vibrator.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val pattern = longArrayOf(0, 70, 40, 90)
                val amplitudes = intArrayOf(0, 200, 0, 255)
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(120)
            }
        } catch (_: Exception) {}
    }

    // --- Synthetic Audio Waveform Generators (8-bit Mono PCM, 22050Hz) ---

    private fun generatePistolPcm(): ByteArray {
        val durationMs = 120
        val numSamples = (sampleRate * durationMs / 1000)
        val buffer = ByteArray(numSamples)
        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val decay = (1f - progress) * (1f - progress)
            val noise = (Random.nextFloat() * 2f - 1f) * 0.7f
            val freq = 440.0 * (1.0 - progress * 0.8)
            val sine = sin(2.0 * Math.PI * freq * i / sampleRate).toFloat() * 0.3f
            val sample = ((noise + sine) * decay).coerceIn(-1f, 1f)
            buffer[i] = ((sample * 120f) + 128f).toInt().toByte()
        }
        return buffer
    }

    private fun generateShotgunPcm(): ByteArray {
        val durationMs = 280
        val numSamples = (sampleRate * durationMs / 1000)
        val buffer = ByteArray(numSamples)
        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val decay = (1f - progress).let { it * it * it }
            val noise = (Random.nextFloat() * 2f - 1f) * 0.85f
            val lowFreq = 130.0 * (1.0 - progress * 0.7)
            val bass = sin(2.0 * Math.PI * lowFreq * i / sampleRate).toFloat() * 0.5f
            val sample = ((noise + bass) * decay).coerceIn(-1f, 1f)
            buffer[i] = ((sample * 125f) + 128f).toInt().toByte()
        }
        return buffer
    }

    private fun generateRiflePcm(): ByteArray {
        val durationMs = 85
        val numSamples = (sampleRate * durationMs / 1000)
        val buffer = ByteArray(numSamples)
        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val decay = 1f - progress
            val noise = (Random.nextFloat() * 2f - 1f) * 0.75f
            val punch = sin(2.0 * Math.PI * 300.0 * i / sampleRate).toFloat() * 0.4f
            val sample = ((noise + punch) * decay).coerceIn(-1f, 1f)
            buffer[i] = ((sample * 115f) + 128f).toInt().toByte()
        }
        return buffer
    }

    private fun generateSniperPcm(): ByteArray {
        val durationMs = 380
        val numSamples = (sampleRate * durationMs / 1000)
        val buffer = ByteArray(numSamples)
        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val decay = (1f - progress) * (1f - progress)
            val noise = (Random.nextFloat() * 2f - 1f) * 0.6f
            val tone = sin(2.0 * Math.PI * (500.0 * (1.0 - progress * 0.9)) * i / sampleRate).toFloat() * 0.6f
            val sample = ((noise + tone) * decay).coerceIn(-1f, 1f)
            buffer[i] = ((sample * 126f) + 128f).toInt().toByte()
        }
        return buffer
    }

    private fun generateExplosionPcm(): ByteArray {
        val durationMs = 450
        val numSamples = (sampleRate * durationMs / 1000)
        val buffer = ByteArray(numSamples)
        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val decay = (1f - progress) * (1f - progress)
            val rumble = (Random.nextFloat() * 2f - 1f) * 0.9f
            val subBass = sin(2.0 * Math.PI * (80.0 * (1.0 - progress * 0.5)) * i / sampleRate).toFloat() * 0.6f
            val sample = ((rumble + subBass) * decay).coerceIn(-1f, 1f)
            buffer[i] = ((sample * 125f) + 128f).toInt().toByte()
        }
        return buffer
    }

    private fun generatePlasmaPcm(): ByteArray {
        val durationMs = 160
        val numSamples = (sampleRate * durationMs / 1000)
        val buffer = ByteArray(numSamples)
        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val decay = 1f - progress
            val chirp = sin(2.0 * Math.PI * (900.0 - 550.0 * progress) * i / sampleRate).toFloat()
            val sample = (chirp * decay).coerceIn(-1f, 1f)
            buffer[i] = ((sample * 110f) + 128f).toInt().toByte()
        }
        return buffer
    }

    private fun generateReloadPcm(): ByteArray {
        val durationMs = 220
        val numSamples = (sampleRate * durationMs / 1000)
        val buffer = ByteArray(numSamples)
        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val click1 = if (progress in 0.1f..0.25f) (Random.nextFloat() * 2f - 1f) * 0.8f else 0f
            val click2 = if (progress in 0.7f..0.85f) (Random.nextFloat() * 2f - 1f) * 0.9f else 0f
            val sample = (click1 + click2).coerceIn(-1f, 1f)
            buffer[i] = ((sample * 110f) + 128f).toInt().toByte()
        }
        return buffer
    }

    private fun generateHitPcm(): ByteArray {
        val durationMs = 60
        val numSamples = (sampleRate * durationMs / 1000)
        val buffer = ByteArray(numSamples)
        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val decay = 1f - progress
            val squish = (Random.nextFloat() * 2f - 1f) * 0.6f +
                    sin(2.0 * Math.PI * 180.0 * i / sampleRate).toFloat() * 0.4f
            val sample = (squish * decay).coerceIn(-1f, 1f)
            buffer[i] = ((sample * 105f) + 128f).toInt().toByte()
        }
        return buffer
    }

    private fun generateWaveFanfarePcm(): ByteArray {
        val durationMs = 500
        val numSamples = (sampleRate * durationMs / 1000)
        val buffer = ByteArray(numSamples)
        val freqs = arrayOf(440.0, 554.37, 659.25, 880.0)
        val part = numSamples / 4
        for (i in 0 until numSamples) {
            val noteIdx = (i / part).coerceIn(0, 3)
            val f = freqs[noteIdx]
            val localProgress = (i % part).toFloat() / part
            val env = (1f - localProgress * 0.4f)
            val sample = (sin(2.0 * Math.PI * f * i / sampleRate).toFloat() * 0.6f * env).coerceIn(-1f, 1f)
            buffer[i] = ((sample * 115f) + 128f).toInt().toByte()
        }
        return buffer
    }

    private fun generateGameOverPcm(): ByteArray {
        val durationMs = 600
        val numSamples = (sampleRate * durationMs / 1000)
        val buffer = ByteArray(numSamples)
        val freqs = arrayOf(330.0, 311.13, 293.66, 220.0)
        val part = numSamples / 4
        for (i in 0 until numSamples) {
            val noteIdx = (i / part).coerceIn(0, 3)
            val f = freqs[noteIdx]
            val localProgress = (i % part).toFloat() / part
            val env = (1f - localProgress * 0.5f)
            val sample = (sin(2.0 * Math.PI * f * i / sampleRate).toFloat() * 0.7f * env).coerceIn(-1f, 1f)
            buffer[i] = ((sample * 120f) + 128f).toInt().toByte()
        }
        return buffer
    }
}
