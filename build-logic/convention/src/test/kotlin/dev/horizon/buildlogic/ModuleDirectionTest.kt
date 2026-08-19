package dev.horizon.buildlogic

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ModuleDirectionTest {

    @Test
    fun `core model and core common depend on nothing`() {
        assertTrue(ModuleDirection.allowedFor(":core:model").orEmpty().isEmpty())
        assertTrue(ModuleDirection.allowedFor(":core:common").orEmpty().isEmpty())
    }

    @Test
    fun `core tokens reaches core model only`() {
        assertEqualsSet(setOf(":core:model"), ModuleDirection.allowedFor(":core:tokens"))
    }

    @Test
    fun `a surface may reach core tokens`() {
        // The widget, tile, and notification all render the ramp, so they need
        // the tokens. docs/architecture.md explains the split.
        assertTrue(":core:tokens" in ModuleDirection.allowedFor(":surface:widget").orEmpty())
    }

    @Test
    fun `a surface may never reach core design`() {
        // Section 3 of scaffolding.md. core/design carries Compose UI, which has
        // no business on a RemoteViews classpath.
        listOf(":surface:overlay", ":surface:notification", ":surface:tile", ":surface:widget")
            .forEach { assertFalse(":core:design" in ModuleDirection.allowedFor(it).orEmpty(), it) }
    }

    @Test
    fun `a feature may reach core design`() {
        assertTrue(":core:design" in ModuleDirection.allowedFor(":feature:home").orEmpty())
    }

    @Test
    fun `no layer may reach a sibling feature or surface`() {
        val everyRule = listOf(
            ":core:model", ":core:common", ":core:tokens", ":core:solar",
            ":core:design", ":core:data", ":feature:home", ":surface:overlay",
        )
        everyRule.forEach { path ->
            ModuleDirection.allowedFor(path).orEmpty().forEach { target ->
                assertTrue(
                    target.startsWith(":core:"),
                    "$path is permitted to depend on $target, which is not a core module",
                )
            }
        }
    }

    @Test
    fun `app and the test modules are unconstrained`() {
        assertNull(ModuleDirection.allowedFor(":app"))
        assertNull(ModuleDirection.allowedFor(":benchmark"))
        assertNull(ModuleDirection.allowedFor(":baselineprofile"))
    }

    private fun assertEqualsSet(expected: Set<String>, actual: Set<String>?) {
        assertTrue(expected == actual, "expected $expected but was $actual")
    }
}
