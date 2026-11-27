package dev.horizon.core.solar

import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToLong
import kotlin.math.sin

private const val DEGREES_PER_HOUR = 15.0
private const val DAY_HOURS = 24.0
private const val CIRCLE = 360.0
private const val ESTIMATED_DAWN = 6.0
private const val ESTIMATED_DUSK = 18.0
private const val DAILY_ANOMALY = 0.9856
private const val ANOMALY_OFFSET = 3.289
private const val FIRST_HARMONIC = 1.916
private const val SECOND_HARMONIC = 0.020
private const val PERIHELION = 282.634
private const val COS_OBLIQUITY = 0.91764
private const val SIN_OBLIQUITY = 0.39782
private const val APPARENT_ZENITH = 90.833
private const val DAILY_TIME_DRIFT = 0.06571
private const val MEAN_TIME_OFFSET = 6.622
private const val SECONDS_PER_HOUR = 3600

/** Local sunrise or sunset, or null on a polar day or night. */
public fun sunEvent(date: LocalDate, zone: ZoneId, lat: Double, lon: Double, rise: Boolean): ZonedDateTime? {
    val longitudeHour = lon / DEGREES_PER_HOUR
    val day = date.dayOfYear + ((if (rise) ESTIMATED_DAWN else ESTIMATED_DUSK) - longitudeHour) / DAY_HOURS
    val anomaly = DAILY_ANOMALY * day - ANOMALY_OFFSET
    fun sinDeg(a: Double) = sin(Math.toRadians(a))
    fun cosDeg(a: Double) = cos(Math.toRadians(a))
    val correction = FIRST_HARMONIC * sinDeg(anomaly) + SECOND_HARMONIC * sinDeg(2 * anomaly)
    val longitude = (anomaly + correction + PERIHELION).mod(CIRCLE)
    val ascensionAngle = Math.toDegrees(atan2(COS_OBLIQUITY * sinDeg(longitude), cosDeg(longitude)))
    val ascension = ascensionAngle.mod(CIRCLE) / DEGREES_PER_HOUR
    val declination = SIN_OBLIQUITY * sinDeg(longitude)
    val hourCos = (cosDeg(APPARENT_ZENITH) - declination * sinDeg(lat)) / (cos(asin(declination)) * cosDeg(lat))
    if (hourCos !in -1.0..1.0) return null
    val angle = Math.toDegrees(acos(hourCos))
    val hourAngle = (if (rise) CIRCLE - angle else angle) / DEGREES_PER_HOUR
    val meanTime = hourAngle + ascension - DAILY_TIME_DRIFT * day - MEAN_TIME_OFFSET
    val utc = (meanTime - longitudeHour).mod(DAY_HOURS)
    val result = date.atStartOfDay(ZoneOffset.UTC).plusSeconds((utc * SECONDS_PER_HOUR).roundToLong()).withZoneSameInstant(zone)
    return result.plusDays(date.toEpochDay() - result.toLocalDate().toEpochDay())
}
