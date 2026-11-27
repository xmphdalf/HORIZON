package dev.horizon.core.solar

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlin.math.abs
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SunTest {
    @Test
    fun solsticeEventsStayOnTheRequestedLocalDayAcrossTimeZones() {
        val day = LocalDate.of(2026, 6, 21)
        listOf(
            Triple(23.22 to 72.64, "Asia/Kolkata", "05:54" to "19:29"),
            Triple(51.51 to -0.13, "Europe/London", "04:43" to "21:21"),
            Triple(40.71 to -74.01, "America/New_York", "05:25" to "20:31"),
            Triple(-33.87 to 151.21, "Australia/Sydney", "07:00" to "16:54"),
            Triple(-33.92 to 18.42, "Africa/Johannesburg", "07:52" to "17:45"),
            Triple(35.68 to 139.65, "Asia/Tokyo", "04:25" to "19:00"),
        ).forEach { (point, zone, times) ->
            for (rise in listOf(true, false)) {
                val event = requireNotNull(sunEvent(day, ZoneId.of(zone), point.first, point.second, rise))
                val expected = LocalTime.parse(if (rise) times.first else times.second)
                assertTrue(event.toLocalDate() == day)
                assertTrue(abs(event.toLocalTime().toSecondOfDay() - expected.toSecondOfDay()) < 600, "$zone $event")
            }
        }
        assertNull(sunEvent(day, ZoneId.of("Europe/Oslo"), 69.65, 18.95, true))
    }
}
