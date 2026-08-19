package dev.horizon.core.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class DimPercentTest {

    @Test
    fun `holds a value inside the supported range`() {
        assertEquals(55, DimPercent(55).value)
    }

    @Test
    fun `rejects a value below the safety floor`() {
        // requirement.md F-08: the screen must never get dark enough that the
        // user cannot reach a disable control.
        assertThrows<IllegalArgumentException> { DimPercent(DimPercent.MIN - 1) }
    }

    @Test
    fun `rejects a value above full brightness`() {
        assertThrows<IllegalArgumentException> { DimPercent(DimPercent.MAX + 1) }
    }
}
