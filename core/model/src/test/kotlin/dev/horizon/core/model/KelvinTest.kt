package dev.horizon.core.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class KelvinTest {

    @Test
    fun `holds a value inside the supported range`() {
        assertEquals(2400, Kelvin(2400).value)
    }

    @Test
    fun `rejects a value below the preset floor`() {
        assertThrows<IllegalArgumentException> { Kelvin(Kelvin.MIN - 1) }
    }

    @Test
    fun `rejects a value above the supported ceiling`() {
        assertThrows<IllegalArgumentException> { Kelvin(Kelvin.MAX + 1) }
    }

    @Test
    fun `orders warmer temperatures below cooler ones`() {
        assertTrue(Kelvin(1700) < Kelvin(3400))
    }

    @Test
    fun `keeps the drag range inside the preset range`() {
        // requirement.md section 15.5 records this split deliberately: the
        // Migraine preset at 1000 K sits below the reach of the gesture.
        assertTrue(Kelvin.DRAG_MIN > Kelvin.MIN)
        assertTrue(Kelvin.DRAG_MAX < Kelvin.MAX)
    }
}
