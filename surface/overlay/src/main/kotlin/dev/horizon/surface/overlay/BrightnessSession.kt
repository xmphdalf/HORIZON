package dev.horizon.surface.overlay

import android.content.Context
import android.provider.Settings.System
import kotlin.math.roundToInt

internal class BrightnessSession(context: Context) {
    private val resolver = context.contentResolver
    private val recovery = context.getSharedPreferences("brightness_recovery", Context.MODE_PRIVATE)
    private var original = recovery.getInt("value", -1)
    private var mode = recovery.getInt("mode", System.SCREEN_BRIGHTNESS_MODE_MANUAL)

    fun capture(): Boolean {
        if (original >= 0) return true
        val value = System.getInt(resolver, System.SCREEN_BRIGHTNESS, -1)
        if (value < 0) return false
        mode = System.getInt(resolver, System.SCREEN_BRIGHTNESS_MODE, System.SCREEN_BRIGHTNESS_MODE_MANUAL)
        // A killed process has no final callback. Persist before the first system setting changes.
        if (!recovery.edit().putInt("value", value).putInt("mode", mode).commit()) return false
        original = value
        return true
    }

    fun apply(percent: Int): Boolean {
        if (original < 0) return false
        val target = (original * percent / 100f).roundToInt().coerceAtLeast(minOf(original, 20))
        return System.putInt(resolver, System.SCREEN_BRIGHTNESS_MODE, System.SCREEN_BRIGHTNESS_MODE_MANUAL) &&
            System.putInt(resolver, System.SCREEN_BRIGHTNESS, target)
    }

    fun restore() {
        if (original < 0) return
        if (System.putInt(resolver, System.SCREEN_BRIGHTNESS, original) &&
            System.putInt(resolver, System.SCREEN_BRIGHTNESS_MODE, mode)
        ) {
            original = -1
            recovery.edit().clear().apply()
        }
    }
}
