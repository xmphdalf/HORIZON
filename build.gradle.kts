// Declaring the Kotlin plugin here puts KGP on the build classpath, which
// upgrades the Kotlin that AGP 9 carries built in from its bundled 2.2.10 to
// the version pinned in the catalog. Verified in Phase 0: without this, every
// module silently compiles against 2.2.10.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.androidx.baselineprofile) apply false
}
