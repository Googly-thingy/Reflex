package com.example.hardware

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class HapticManager(context: Context) {

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

    fun hasVibrator(): Boolean {
        return vibrator?.hasVibrator() == true
    }

    /**
     * Immediate stimulus vibration: Direct linear motor trigger with zero delay.
     */
    fun triggerStimulus(durationMs: Long = 40L, amplitude: Int = 255) {
        val vib = vibrator ?: return
        if (!vib.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val clampedAmp = amplitude.coerceIn(1, 255)
                val effect = VibrationEffect.createOneShot(durationMs.coerceAtLeast(10L), clampedAmp)
                vib.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(durationMs)
            }
        } catch (_: Exception) {
            // Graceful fallback
        }
    }

    /**
     * Penalty vibration for false starts / early volume button presses.
     */
    fun triggerFalseStartPenalty() {
        val vib = vibrator ?: return
        if (!vib.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Double sharp buzz pattern: 0ms delay, 120ms buzz, 60ms gap, 120ms buzz
                val pattern = longArrayOf(0, 120, 60, 120)
                val amplitudes = intArrayOf(0, 255, 0, 255)
                val effect = VibrationEffect.createWaveform(pattern, amplitudes, -1)
                vib.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(longArrayOf(0, 120, 60, 120), -1)
            }
        } catch (_: Exception) {
            // Graceful fallback
        }
    }

    /**
     * Micro click confirmation when arming or readying.
     */
    fun triggerArmConfirm() {
        val vib = vibrator ?: return
        if (!vib.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vib.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vib.vibrate(VibrationEffect.createOneShot(15L, 160))
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(15L)
            }
        } catch (_: Exception) {
            // Graceful fallback
        }
    }

    /**
     * Crisp success click after successful reflex response recorded.
     */
    fun triggerSuccessPulse() {
        val vib = vibrator ?: return
        if (!vib.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vib.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vib.vibrate(VibrationEffect.createOneShot(25L, 220))
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(25L)
            }
        } catch (_: Exception) {
            // Graceful fallback
        }
    }

    fun cancel() {
        try {
            vibrator?.cancel()
        } catch (_: Exception) {
            // Graceful fallback
        }
    }
}
