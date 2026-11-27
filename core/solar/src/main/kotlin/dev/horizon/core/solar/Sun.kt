package dev.horizon.core.solar

import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import kotlin.math.*

/** Local sunrise or sunset, or null on a polar day or night. */
public fun sunEvent(date: LocalDate, zone: ZoneId, lat: Double, lon: Double, rise: Boolean): ZonedDateTime? {
    val longitudeHour = lon / 15
    val day = date.dayOfYear + ((if (rise) 6.0 else 18.0) - longitudeHour) / 24
    val anomaly = .9856 * day - 3.289
    fun sinDeg(a: Double) = sin(Math.toRadians(a))
    fun cosDeg(a: Double) = cos(Math.toRadians(a))
    val longitude = (anomaly + 1.916 * sinDeg(anomaly) + .020 * sinDeg(2 * anomaly) + 282.634).mod(360.0)
    val ascension = Math.toDegrees(atan2(.91764 * sinDeg(longitude), cosDeg(longitude))).mod(360.0) / 15
    val declination = .39782 * sinDeg(longitude)
    val hourCos = (cosDeg(90.833) - declination * sinDeg(lat)) / (cos(asin(declination)) * cosDeg(lat))
    if (hourCos !in -1.0..1.0) return null
    val angle = Math.toDegrees(acos(hourCos))
    val utc = ((if (rise) 360 - angle else angle) / 15 + ascension - .06571 * day - 6.622 - longitudeHour).mod(24.0)
    val result = date.atStartOfDay(ZoneOffset.UTC).plusSeconds((utc * 3600).roundToLong()).withZoneSameInstant(zone)
    return result.plusDays(date.toEpochDay() - result.toLocalDate().toEpochDay())
}
