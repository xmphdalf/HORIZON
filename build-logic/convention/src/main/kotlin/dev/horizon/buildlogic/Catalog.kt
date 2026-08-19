package dev.horizon.buildlogic

import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType

/**
 * The `libs` version catalog.
 *
 * Precompiled script plugins do not get the generated `LibrariesForLibs`
 * accessor, so they read the catalog through this supported API instead of the
 * generated-class workaround, which breaks whenever Gradle moves the class.
 */
public val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

/** Returns the required version for [name], failing loudly if the alias is absent. */
public fun VersionCatalog.version(name: String): String =
    findVersion(name).orElseThrow { IllegalStateException("No version '$name' in libs.versions.toml") }
        .requiredVersion

/** Returns the library for [name], failing loudly if the alias is absent. */
public fun VersionCatalog.lib(name: String): Provider<MinimalExternalModuleDependency> =
    findLibrary(name).orElseThrow { IllegalStateException("No library '$name' in libs.versions.toml") }
