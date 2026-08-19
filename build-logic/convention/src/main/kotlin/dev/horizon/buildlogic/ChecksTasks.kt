package dev.horizon.buildlogic

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction

/**
 * Fails when a module depends on a module its layer may not reach.
 *
 * The inputs are populated after the project is evaluated, because a precompiled
 * script plugin applies before the module's own `dependencies` block runs and
 * would otherwise always see an empty set.
 */
public abstract class ModuleDirectionTask : DefaultTask() {

    /** Project paths this module declares on its production configurations. */
    @get:Input
    public abstract val declared: SetProperty<String>

    /** Project paths this module's layer permits. */
    @get:Input
    public abstract val permitted: SetProperty<String>

    /**
     * False for a module whose layer is unconstrained, such as `:app`. The task
     * still runs so its inputs stay part of the up-to-date check, but does
     * nothing.
     *
     * This is a task input rather than an `onlyIf { }` predicate because an
     * `onlyIf` spec is stored as part of the configuration cache entry, and a
     * closure defined inside a precompiled script plugin can drag in a
     * reference to the compiled script class itself, which the configuration
     * cache refuses to serialise. A plain `Property` has no such problem.
     */
    @get:Input
    public abstract val constrained: Property<Boolean>

    /** The path of the module being checked, used in the failure message. */
    @get:Input
    public abstract val projectPath: Property<String>

    /** Compares the declared set against the permitted set. */
    @TaskAction
    public fun check() {
        if (!constrained.get()) return

        val illegal = declared.get() - permitted.get()
        if (illegal.isEmpty()) return

        error(
            buildString {
                appendLine("${projectPath.get()} declares a dependency its layer does not permit:")
                illegal.sorted().forEach { appendLine("  $it") }
                appendLine()
                val allowed = permitted.get().sorted().joinToString(", ").ifEmpty { "nothing" }
                appendLine("Permitted: $allowed")
                appendLine("See docs/architecture.md and section 3 of scaffolding.md.")
            },
        )
    }
}

/**
 * Fails on the `!!` operator, which section 6.2 of scaffolding.md forbids.
 *
 * detekt's precise rule needs type resolution, which is not enabled here, so
 * this scans the source text instead. Comments and string literals are blanked
 * first, because an error message reading "Careful!!" is not an operator and a
 * gate that rejects valid code is worse than the gap it closes.
 */
public abstract class NoBangBangTask : DefaultTask() {

    /** Kotlin sources to scan. */
    @get:InputFiles
    @get:SkipWhenEmpty
    @get:PathSensitive(PathSensitivity.RELATIVE)
    public abstract val sources: ConfigurableFileCollection

    /** Scans every source file for the operator. */
    @TaskAction
    public fun check() {
        val offenders = sources.files
            .filter { it.isFile }
            .flatMap { file ->
                val original = file.readLines()
                BangBangScanner.offendingLines(file.readText())
                    .map { number -> "${file.path}:$number: ${original[number - 1].trim()}" }
            }

        if (offenders.isEmpty()) return

        error(
            buildString {
                appendLine("The !! operator is forbidden. Section 6.2 of scaffolding.md:")
                appendLine("if a value cannot be null, model it as non-null; if it can, handle it.")
                appendLine()
                offenders.forEach { appendLine("  $it") }
            },
        )
    }
}

/**
 * Finds the `!!` operator in Kotlin source, ignoring comments and literals.
 *
 * Separated from the task so it can be unit tested without a Gradle build.
 */
public object BangBangScanner {

    /** Returns the 1-based line numbers on which the operator appears. */
    public fun offendingLines(source: String): List<Int> {
        val masked = mask(source)
        val offenders = mutableListOf<Int>()
        masked.lineSequence().forEachIndexed { index, line ->
            if ("!!" in line) offenders += index + 1
        }
        return offenders
    }

    /**
     * Replaces the contents of comments, string literals, and character literals
     * with spaces, preserving line structure so line numbers still line up.
     */
    @Suppress("CyclomaticComplexMethod", "NestedBlockDepth")
    private fun mask(source: String): String {
        val out = StringBuilder(source.length)
        var i = 0

        fun peek(offset: Int): Char? = source.getOrNull(i + offset)

        while (i < source.length) {
            val c = source[i]
            when {
                c == '/' && peek(1) == '/' -> {
                    while (i < source.length && source[i] != '\n') { out.append(' '); i++ }
                }

                c == '/' && peek(1) == '*' -> {
                    while (i < source.length && !(source[i] == '*' && peek(1) == '/')) {
                        out.append(if (source[i] == '\n') '\n' else ' ')
                        i++
                    }
                    repeat(minOf(2, source.length - i)) { out.append(' '); i++ }
                }

                c == '"' && peek(1) == '"' && peek(2) == '"' -> {
                    repeat(3) { out.append(' '); i++ }
                    while (i < source.length && !(source[i] == '"' && peek(1) == '"' && peek(2) == '"')) {
                        out.append(if (source[i] == '\n') '\n' else ' ')
                        i++
                    }
                    repeat(minOf(3, source.length - i)) { out.append(' '); i++ }
                }

                c == '"' -> {
                    out.append(' '); i++
                    while (i < source.length && source[i] != '"') {
                        if (source[i] == '\\') { out.append(' '); i++ }
                        if (i < source.length) { out.append(if (source[i] == '\n') '\n' else ' '); i++ }
                    }
                    if (i < source.length) { out.append(' '); i++ }
                }

                c == '\'' -> {
                    out.append(' '); i++
                    while (i < source.length && source[i] != '\'') {
                        if (source[i] == '\\') { out.append(' '); i++ }
                        if (i < source.length) { out.append(' '); i++ }
                    }
                    if (i < source.length) { out.append(' '); i++ }
                }

                else -> { out.append(c); i++ }
            }
        }
        return out.toString()
    }
}

/**
 * Fails on any analytics, advertising, crash-reporting, or networking dependency
 * anywhere on a runtime classpath.
 */
public abstract class DependencyAuditTask : DefaultTask() {

    /** Roots of each resolved runtime classpath. */
    @get:Input
    public abstract val rootComponents: ListProperty<ResolvedComponentResult>

    /** The path of the module being checked, used in the failure message. */
    @get:Input
    public abstract val projectPath: Property<String>

    /** Walks every resolved graph and matches against the deny list. */
    @TaskAction
    public fun check() {
        val coordinates = mutableSetOf<String>()
        rootComponents.get().forEach { root -> collect(root, coordinates, mutableSetOf()) }

        val violations = DependencyAudit.violations(coordinates)
        if (violations.isEmpty()) return

        error(
            buildString {
                appendLine("${projectPath.get()} pulls in a dependency the four commitments forbid:")
                violations.forEach { appendLine("  $it") }
                appendLine()
                appendLine("See requirement.md section 2 and section 8.")
            },
        )
    }

    private fun collect(
        component: ResolvedComponentResult,
        into: MutableSet<String>,
        seen: MutableSet<String>,
    ) {
        val id = component.id.displayName
        if (!seen.add(id)) return
        into += id
        component.dependencies
            .filterIsInstance<org.gradle.api.artifacts.result.ResolvedDependencyResult>()
            .forEach { collect(it.selected, into, seen) }
    }
}
