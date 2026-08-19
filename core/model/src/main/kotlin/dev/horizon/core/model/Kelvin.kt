package dev.horizon.core.model

/**
 * A colour temperature in kelvin.
 *
 * The range is the full preset range from requirement.md F-02, not the range
 * the drag gesture reaches. The gesture bottoms out at [DRAG_MIN] and the
 * Migraine preset sits below it at [MIN], which is why the two are separate
 * constants rather than one.
 */
@JvmInline
public value class Kelvin(
    /** Guaranteed by the constructor to lie in [MIN]..[MAX]. */
    public val value: Int,
) : Comparable<Kelvin> {

    init {
        require(value in MIN..MAX) { "Kelvin must be in $MIN..$MAX, was $value" }
    }

    override fun compareTo(other: Kelvin): Int = value.compareTo(other.value)

    /** Range bounds, shared by the drag gesture, the presets, and the ramp. */
    public companion object {
        /** Coldest supported temperature. requirement.md F-02. */
        public const val MAX: Int = 6500

        /** Warmest supported temperature, reachable only by preset. requirement.md F-02. */
        public const val MIN: Int = 1000

        /** Coldest temperature the drag gesture reaches. */
        public const val DRAG_MAX: Int = 4600

        /** Warmest temperature the drag gesture reaches. requirement.md section 15.5. */
        public const val DRAG_MIN: Int = 1600

        /**
         * The gesture quantises to this step. Chosen so a value never lands on a
         * number the readout cannot show as a stable tabular numeral.
         */
        public const val STEP: Int = 50
    }
}
