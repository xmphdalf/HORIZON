package dev.horizon.buildlogic

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PermissionAuditTest {

    private val doc = """
        | Permission | Why | Optional |
        |---|---|---|
        | `android.permission.SYSTEM_ALERT_WINDOW` | The overlay itself | No |
        | `android.permission.FOREGROUND_SERVICE` | Keeping the overlay alive | No |
    """.trimIndent()

    private val allowed = PermissionAudit.parseAllowed(doc)

    @Test
    fun `parses every permission out of the docs table`() {
        assertEquals(
            setOf(
                "android.permission.SYSTEM_ALERT_WINDOW",
                "android.permission.FOREGROUND_SERVICE",
            ),
            allowed,
        )
    }

    @Test
    fun `parses permissions out of a manifest`() {
        val manifest = """
            <manifest>
              <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
              <uses-permission android:name="android.permission.INTERNET" />
            </manifest>
        """.trimIndent()

        assertEquals(
            setOf("android.permission.SYSTEM_ALERT_WINDOW", PermissionAudit.INTERNET),
            PermissionAudit.parseDeclared(manifest),
        )
    }

    @Test
    fun `accepts a manifest declaring only listed permissions`() {
        val declared = setOf("android.permission.SYSTEM_ALERT_WINDOW")
        assertTrue(PermissionAudit.violations(declared, allowed, APP_ID).isEmpty())
    }

    @Test
    fun `reports INTERNET by naming the commitment it breaks`() {
        val violations = PermissionAudit.violations(setOf(PermissionAudit.INTERNET), allowed, APP_ID)
        assertEquals(1, violations.size)
        assertTrue(violations.single().contains("four commitments"), violations.single())
    }

    @Test
    fun `reports a permission that is absent from the docs table`() {
        val violations = PermissionAudit.violations(setOf("android.permission.CAMERA"), allowed, APP_ID)
        assertEquals(1, violations.size)
        assertTrue(violations.single().contains("android.permission.CAMERA"))
    }

    @Test
    fun `ignores permissions the app defines in its own namespace`() {
        // AGP and androidx inject these. They guard the app's own components
        // rather than requesting a capability, so they are not part of the list
        // a user reads in requirement.md section 8.
        val injected = "$APP_ID.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION"
        assertTrue(PermissionAudit.violations(setOf(injected), allowed, APP_ID).isEmpty())
    }

    @Test
    fun `still reports a permission in someone else's namespace`() {
        val foreign = "com.google.android.c2dm.permission.RECEIVE"
        assertEquals(1, PermissionAudit.violations(setOf(foreign), allowed, APP_ID).size)
    }

    private companion object {
        const val APP_ID = "dev.horizon.filter"
    }
}
