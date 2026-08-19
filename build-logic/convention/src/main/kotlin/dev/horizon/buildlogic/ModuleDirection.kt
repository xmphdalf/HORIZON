package dev.horizon.buildlogic

/**
 * The permitted dependency directions, from section 3 of scaffolding.md as
 * amended by the `core/tokens` split recorded in docs/architecture.md.
 *
 * A module may depend on the projects its layer allows and nothing else.
 * Modules point downward only, and never at a sibling in `feature/` or
 * `surface/`.
 */
public object ModuleDirection {

    private val CORE_LEAF = emptySet<String>()
    private val MODEL = setOf(":core:model")
    private val MODEL_AND_COMMON = setOf(":core:model", ":core:common")
    private val MODEL_AND_TOKENS = setOf(":core:model", ":core:tokens")

    private val FEATURE_ALLOWED = setOf(
        ":core:model",
        ":core:common",
        ":core:tokens",
        ":core:design",
        ":core:data",
    )

    // core/design is deliberately absent. A widget, tile, or notification takes
    // its colours from core/tokens; pulling core/design in would put Compose UI
    // on a RemoteViews classpath.
    private val SURFACE_ALLOWED = setOf(
        ":core:model",
        ":core:common",
        ":core:tokens",
        ":core:data",
    )

    /** Exact rules for the modules that are not covered by a layer prefix. */
    private val EXACT: Map<String, Set<String>> = mapOf(
        ":core:model" to CORE_LEAF,
        ":core:common" to CORE_LEAF,
        ":core:tokens" to MODEL,
        ":core:solar" to MODEL,
        ":core:design" to MODEL_AND_TOKENS,
        ":core:data" to MODEL_AND_COMMON,
    )

    /**
     * Returns the project paths [projectPath] is allowed to depend on, or null
     * when the module is unconstrained.
     *
     * `:app` is unconstrained because assembling the graph is its job.
     * `:benchmark` and `:baselineprofile` are unconstrained because they target
     * the installed app rather than depending on production modules.
     */
    public fun allowedFor(projectPath: String): Set<String>? = when {
        EXACT.containsKey(projectPath) -> EXACT.getValue(projectPath)
        projectPath.startsWith(":feature:") -> FEATURE_ALLOWED
        projectPath.startsWith(":surface:") -> SURFACE_ALLOWED
        else -> null
    }
}
