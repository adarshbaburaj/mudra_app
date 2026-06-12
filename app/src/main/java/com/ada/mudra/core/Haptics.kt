package com.ada.mudra.core

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Strong, single-pulse haptic used for every senior-facing tap so the person
 * always feels that the phone registered their touch.
 */
object Haptics {

    fun strongTap(context: Context) {
        val vibrator = vibrator(context) ?: return
        if (!vibrator.hasVibrator()) return
        vibrator.vibrate(VibrationEffect.createOneShot(80, 255))
    }

    private fun vibrator(context: Context): Vibrator? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)
                ?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
}
