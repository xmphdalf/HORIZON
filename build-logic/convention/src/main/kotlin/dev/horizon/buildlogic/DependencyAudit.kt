package dev.horizon.buildlogic

/**
 * The dependency deny list.
 *
 * requirement.md section 2 commits to no advertising and no data collection, and
 * section 8 to no network access at all. A dependency is the quiet way both
 * break: nothing in the source has to change for an analytics SDK to start
 * transmitting.
 *
 * Matching is on `group:name` prefixes, so a new artifact inside an already
 * denied group is caught without editing this list.
 */
public object DependencyAudit {

    /** Group or module prefixes that may never appear on any classpath. */
    public val DENIED: List<DeniedCoordinate> = listOf(
        // Analytics, attribution, and product metrics.
        DeniedCoordinate("com.google.firebase:firebase-analytics", "analytics"),
        DeniedCoordinate("com.google.android.gms:play-services-analytics", "analytics"),
        DeniedCoordinate("com.google.android.gms:play-services-measurement", "analytics"),
        DeniedCoordinate("com.mixpanel", "analytics"),
        DeniedCoordinate("com.amplitude", "analytics"),
        DeniedCoordinate("com.segment", "analytics"),
        DeniedCoordinate("com.posthog", "analytics"),
        DeniedCoordinate("com.appsflyer", "attribution"),
        DeniedCoordinate("com.adjust", "attribution"),

        // Advertising.
        DeniedCoordinate("com.google.android.gms:play-services-ads", "advertising"),
        DeniedCoordinate("com.applovin", "advertising"),
        DeniedCoordinate("com.unity3d.ads", "advertising"),
        DeniedCoordinate("com.facebook.android:audience-network", "advertising"),

        // Crash reporting that transmits. An on-device log the user exports by
        // hand is the alternative left open in requirement.md section 15.7.
        DeniedCoordinate("com.google.firebase:firebase-crashlytics", "crash reporting"),
        DeniedCoordinate("io.sentry", "crash reporting"),
        DeniedCoordinate("com.bugsnag", "crash reporting"),
        DeniedCoordinate("ch.acra", "crash reporting"),

        // Networking. Horizon declares no INTERNET permission, so any of these is
        // either dead weight in the APK or a permission request waiting to happen.
        DeniedCoordinate("com.squareup.okhttp3", "networking"),
        DeniedCoordinate("com.squareup.retrofit2", "networking"),
        DeniedCoordinate("io.ktor:ktor-client", "networking"),
        DeniedCoordinate("com.android.volley", "networking"),
        DeniedCoordinate("io.coil-kt", "networking"),
        DeniedCoordinate("com.github.bumptech.glide", "networking"),
        DeniedCoordinate("com.squareup.picasso", "networking"),
    )

    /**
     * Returns the failure messages for [coordinates], each formatted
     * `group:name:version`, empty when nothing is denied.
     */
    public fun violations(coordinates: Iterable<String>): List<String> =
        coordinates.asSequence()
            .mapNotNull { coordinate ->
                DENIED.firstOrNull { coordinate.startsWith(it.prefix) }
                    ?.let { "$coordinate is a ${it.reason} dependency" }
            }
            .distinct()
            .sorted()
            .toList()
}

/** A denied `group:name` prefix and why it is denied. */
public data class DeniedCoordinate(
    /** Matched against the start of a `group:name:version` string. */
    public val prefix: String,
    /** Named in the failure message so the commitment being protected is clear. */
    public val reason: String,
)
