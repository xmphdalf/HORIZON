package dev.horizon.core.model

/**
 * Screen brightness as a percentage of full.
 *
 * The floor is 8 rather than 0 because requirement.md F-08 requires that no
 * combination of settings leaves the user unable to find the disable control.
 */
@JvmInline
public value class DimPercent(
    /** Guaranteed by the constructor to lie in [MIN]..[MAX]. */
    public val value: Int,
) : Comparable<DimPercent> {

    init {
        require(value in MIN..MAX) { "DimPercent must be in $MIN..$MAX, was $value" }
    }

    override fun compareTo(other: DimPercent): Int = value.compareTo(other.value)

    /** Range bounds, shared by the drag gesture and the brightness controller. */
    public companion object {
        /** Full brightness. */
        public const val MAX: Int = 100

        /** The hard safety floor from requirement.md F-08. */
        public const val MIN: Int = 8
    }
}
