package com.grimorio.rpg.core.haptic

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Gerenciador de feedback tátil para rolagens de dados e interações com a mesa.
 */
interface HapticManager {
    fun vibrateRoll()
    fun vibrateCriticalHit()
    fun vibrateCriticalFail()
    fun vibrateClick()
}

class AndroidHapticManager(context: Context) : HapticManager {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    override fun vibrateRoll() {
        if (vibrator?.hasVibrator() != true) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (vibrator.hasAmplitudeControl()) {
                val timings = longArrayOf(0, 30, 25, 45)
                val amplitudes = intArrayOf(0, 90, 0, 140)
                vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(50)
        }
    }

    override fun vibrateCriticalHit() {
        if (vibrator?.hasVibrator() != true) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (vibrator.hasAmplitudeControl()) {
                // Pulso rítmico triunfante em 3 tempos crescentes (acerto épico)
                val timings = longArrayOf(0, 60, 40, 80, 50, 160)
                val amplitudes = intArrayOf(0, 120, 0, 180, 0, 255)
                vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 60, 40, 80, 50, 160), -1))
            }
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 60, 40, 80, 50, 160), -1)
        }
    }

    override fun vibrateCriticalFail() {
        if (vibrator?.hasVibrator() != true) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (vibrator.hasAmplitudeControl()) {
                // Impacto pesado e descendente
                val timings = longArrayOf(0, 220, 80, 260)
                val amplitudes = intArrayOf(0, 255, 0, 160)
                vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 220, 80, 260), -1))
            }
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 220, 80, 260), -1)
        }
    }

    override fun vibrateClick() {
        if (vibrator?.hasVibrator() != true) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(15, 80))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(15)
        }
    }
}

class NoOpHapticManager : HapticManager {
    override fun vibrateRoll() = Unit
    override fun vibrateCriticalHit() = Unit
    override fun vibrateCriticalFail() = Unit
    override fun vibrateClick() = Unit
}
