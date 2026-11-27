package dev.horizon.filter

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import dev.horizon.core.solar.sunEvent
import dev.horizon.surface.overlay.FilterService
import java.time.*

internal val cities = mapOf(
    "Gandhinagar" to (23.22 to 72.64), "Ahmedabad" to (23.02 to 72.57), "Mumbai" to (19.08 to 72.88),
    "Bengaluru" to (12.97 to 77.59), "Delhi" to (28.61 to 77.21), "London" to (51.51 to -0.13),
    "New York" to (40.71 to -74.01), "Sydney" to (-33.87 to 151.21),
)

internal class Schedule(private val context: Context) {
    private val prefs = context.getSharedPreferences("manual_filter", Context.MODE_PRIVATE)
    private val alarm = requireNotNull(context.getSystemService(AlarmManager::class.java))
    fun allowed() = Build.VERSION.SDK_INT < 31 || alarm.canScheduleExactAlarms()
    fun event(date: LocalDate, start: Boolean): ZonedDateTime? {
        val zone = ZoneId.systemDefault()
        val inverted = prefs.getBoolean("invert", false)
        val evening = start != inverted
        if (!prefs.getBoolean("solar", false)) {
            val time = prefs.getInt(if (evening) "evening" else "morning", if (evening) 1320 else 450)
            return date.atTime(time / 60, time % 60).atZone(zone)
        }
        val city = prefs.getString("city", "Gandhinagar")
        val point = cities[city] ?: coords(city.orEmpty()) ?: return null
        return sunEvent(date, zone, point.first, point.second, !evening)
            ?.plusMinutes(prefs.getInt(if (evening) "sunset_offset" else "sunrise_offset", 0).toLong())
    }

    fun arm() {
        val now = ZonedDateTime.now()
        for (start in listOf(true, false)) {
            val intent = Intent(context, ScheduleReceiver::class.java).setAction(if (start) "START" else "END")
            val pending = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            alarm.cancel(pending)
            if (allowed() && prefs.getBoolean("schedule", false)) {
                val next = (-1L..8L).mapNotNull { event(now.toLocalDate().plusDays(it), start) }.firstOrNull { it.isAfter(now) }
                if (next != null) alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.toInstant().toEpochMilli(), pending)
            }
        }
    }
}

internal fun coords(s: String): Pair<Double, Double>? {
    val parts = s.split(",").map { it.trim().toDoubleOrNull() }
    val lat = parts.getOrNull(0) ?: return null
    val lon = parts.getOrNull(1) ?: return null
    return if (parts.size == 2 && lat in -90.0..90.0 && lon in -180.0..180.0) lat to lon else null
}

/** Recomputes local-time alarms when the clock or zone changes. */
class ScheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val p = context.getSharedPreferences("manual_filter", Context.MODE_PRIVATE)
        val scheduler = Schedule(context)
        val boot = intent.action == Intent.ACTION_BOOT_COMPLETED
        if (boot && !p.getBoolean("restore", false)) {
            p.edit().putBoolean("filter_running", false).apply()
            scheduler.arm()
            return
        }
        scheduler.arm()
        val active = p.getBoolean("schedule", false)
        when {
            active && intent.action == "END" -> if (p.getBoolean("filter_running", false)) {
                context.startService(Intent(context, FilterService::class.java).setAction("dev.horizon.filter.STOP"))
            } else Unit
            (active && intent.action == "START") || (boot && p.getBoolean("filter_running", false)) -> {
                if (Settings.canDrawOverlays(context) && Settings.System.canWrite(context)) {
                    context.startForegroundService(Intent(context, FilterService::class.java)
                        .putExtra("kelvin", p.getInt("kelvin", 3400)).putExtra("dim", p.getInt("dim", 82))
                        .putExtra("ramp", if (boot) 0 else p.getInt("ramp", 0)))
                }
            }
        }
    }
}
