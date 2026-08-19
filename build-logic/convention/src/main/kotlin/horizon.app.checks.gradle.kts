import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import dev.horizon.buildlogic.PermissionAudit

// Gates that need a built application rather than a module: the merged manifest
// and the release APK. Sections 8.4 and 8.6 of scaffolding.md.

// requirement.md N-03. Raised from 5 MB to accommodate Compose, Glance, and the
// bundled fonts, and recorded in the decision log at requirement.md section 14.
val apkBudgetBytes = 6L * 1024 * 1024

val permissionsDoc = rootProject.layout.projectDirectory.file("docs/permissions.md")

extensions.configure<ApplicationAndroidComponentsExtension>("androidComponents") {
    onVariants { variant ->
        val capitalised = variant.name.replaceFirstChar { it.uppercase() }

        val permissionAudit = tasks.register("permissionAudit$capitalised") {
            group = "verification"
            description = "Fails if the merged manifest declares a permission docs/permissions.md does not list."

            val manifest = variant.artifacts.get(SingleArtifact.MERGED_MANIFEST)
            val doc = permissionsDoc

            // Only the name is captured. Holding the variant itself would drag
            // the whole AGP object graph into the configuration cache entry,
            // which cannot be serialised.
            val variantName = variant.name
            val appId = variant.applicationId

            inputs.file(manifest).withPropertyName("mergedManifest")
            inputs.file(doc).withPropertyName("permissionsDoc")

            doLast {
                val allowed = PermissionAudit.parseAllowed(doc.asFile.readText())
                check(allowed.isNotEmpty()) {
                    "No permissions parsed from ${doc.asFile}. The table format changed; " +
                        "PermissionAudit.TABLE_ROW needs updating."
                }

                val declared = PermissionAudit.parseDeclared(manifest.get().asFile.readText())
                val violations = PermissionAudit.violations(declared, allowed, appId.get())
                if (violations.isNotEmpty()) {
                    error(
                        buildString {
                            appendLine("Permission audit failed for variant $variantName:")
                            violations.forEach { appendLine("  $it") }
                        },
                    )
                }
            }
        }

        tasks.named("check").configure { dependsOn(permissionAudit) }

        if (variant.buildType == "release") {
            tasks.register("apkSizeCheck$capitalised") {
                group = "verification"
                description = "Fails if the release APK exceeds the budget in requirement.md N-03."

                val apkDir = variant.artifacts.get(SingleArtifact.APK)
                val budget = apkBudgetBytes

                inputs.dir(apkDir).withPropertyName("apkDirectory")

                doLast {
                    val apks = apkDir.get().asFile.listFiles { file -> file.extension == "apk" }.orEmpty()
                    check(apks.isNotEmpty()) { "No APK found in ${apkDir.get().asFile}" }

                    apks.forEach { apk ->
                        val size = apk.length()
                        val budgetMb = budget / 1024.0 / 1024.0
                        val sizeMb = size / 1024.0 / 1024.0
                        logger.lifecycle("APK ${apk.name}: %.2f MB of %.2f MB budget".format(sizeMb, budgetMb))
                        if (size > budget) {
                            error(
                                "${apk.name} is %.2f MB, over the %.2f MB budget in requirement.md N-03."
                                    .format(sizeMb, budgetMb),
                            )
                        }
                    }
                }
            }
        }
    }
}
