package dev.horizon.buildlogic

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DependencyAuditTest {

    @Test
    fun `accepts the dependencies the project actually uses`() {
        val clean = listOf(
            "androidx.core:core-ktx:1.19.0",
            "androidx.datastore:datastore:1.2.0",
            "androidx.glance:glance-appwidget:1.1.1",
            "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2",
            "com.google.protobuf:protobuf-javalite:4.32.1",
        )
        assertTrue(DependencyAudit.violations(clean).isEmpty())
    }

    @Test
    fun `rejects a networking library`() {
        val violations = DependencyAudit.violations(listOf("com.squareup.okhttp3:okhttp:4.12.0"))
        assertEquals(1, violations.size)
        assertTrue(violations.single().contains("networking"))
    }

    @Test
    fun `rejects analytics, advertising, and crash reporting`() {
        val denied = listOf(
            "com.google.firebase:firebase-analytics:22.0.0",
            "com.google.android.gms:play-services-ads:23.0.0",
            "com.google.firebase:firebase-crashlytics:19.0.0",
        )
        assertEquals(3, DependencyAudit.violations(denied).size)
    }

    @Test
    fun `catches a new artifact inside an already denied group`() {
        // Matching on prefixes means a group does not need re-listing every time
        // it publishes another artifact.
        val violations = DependencyAudit.violations(listOf("io.sentry:sentry-android-fragment:7.0.0"))
        assertEquals(1, violations.size)
    }

    @Test
    fun `names the reason so the failure explains which commitment is at stake`() {
        val violations = DependencyAudit.violations(listOf("com.appsflyer:af-android-sdk:6.0.0"))
        assertTrue(violations.single().contains("attribution"), violations.single())
    }

    @Test
    fun `reports each coordinate once`() {
        val duplicated = listOf("io.sentry:sentry:7.0.0", "io.sentry:sentry:7.0.0")
        assertEquals(1, DependencyAudit.violations(duplicated).size)
    }
}
