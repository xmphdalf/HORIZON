package dev.horizon.buildlogic

/**
 * Parses the permission table in docs/permissions.md and compares it against the
 * permissions an Android manifest declares.
 *
 * docs/permissions.md is the single source of truth because scaffolding.md
 * section 8 names it as the thing CI checks against. Keeping the list in a
 * generated file instead would let the documentation drift from the gate.
 */
public object PermissionAudit {

    private val TABLE_ROW = Regex("""^\|\s*`(android\.permission\.[A-Z_]+)`\s*\|""")
    private val MANIFEST_PERMISSION = Regex("""<uses-permission[^>]*android:name\s*=\s*"([^"]+)"""")

    /** The permission whose absence is the product claim, not merely a policy. */
    public const val INTERNET: String = "android.permission.INTERNET"

    /** Returns every permission named in the markdown table of [docText]. */
    public fun parseAllowed(docText: String): Set<String> =
        docText.lineSequence()
            .mapNotNull { TABLE_ROW.find(it)?.groupValues?.get(1) }
            .toSet()

    /** Returns every permission declared in [manifestText]. */
    public fun parseDeclared(manifestText: String): Set<String> =
        MANIFEST_PERMISSION.findAll(manifestText)
            .map { it.groupValues[1] }
            .toSet()

    /**
     * Returns the failure messages for [declared] against [allowed], empty when
     * the manifest is clean.
     *
     * INTERNET is reported on its own line before anything else so the failure
     * names the commitment it breaks rather than reading as a list mismatch.
     *
     * Permissions the app defines in its own namespace are skipped. AGP and
     * androidx inject some automatically, such as
     * `<applicationId>.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`. Those are
     * self-grants that guard the app's own components, not capabilities
     * requested from the system, so they are not part of the section 8 list a
     * user reads. A permission in someone else's namespace is still reported.
     */
    public fun violations(
        declared: Set<String>,
        allowed: Set<String>,
        applicationId: String,
    ): List<String> {
        val messages = mutableListOf<String>()
        if (INTERNET in declared) {
            messages += "$INTERNET is declared. Horizon never declares it: " +
                "requirement.md section 8, and the third of the four commitments."
        }
        val selfDefined = declared.filter { it.startsWith("$applicationId.") }.toSet()
        (declared - allowed - INTERNET - selfDefined).sorted().forEach {
            messages += "$it is not listed in docs/permissions.md. " +
                "Add it there and to requirement.md section 8 and the in-app " +
                "permission screen, in this commit, or remove it."
        }
        return messages
    }
}
