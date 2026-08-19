import com.android.build.api.dsl.CommonExtension
import dev.horizon.buildlogic.lib
import dev.horizon.buildlogic.libs

plugins {
    id("org.jetbrains.kotlin.plugin.compose")
}

// Applies to whichever Android extension the module already registered, so the
// same plugin serves an application module and a library module.
// AGP 9 dropped the type parameters CommonExtension carried in AGP 8.
// `this.compose` is qualified because the bare name resolves to the Kotlin
// Compose plugin accessor brought in by the plugins block above.
extensions.configure<CommonExtension>("android") {
    buildFeatures.compose = true
}

dependencies {
    val bom = platform(libs.lib("compose-bom"))
    add("implementation", bom)
    add("androidTestImplementation", bom)

    add("implementation", libs.lib("compose-ui"))
    add("implementation", libs.lib("compose-ui-graphics"))
    add("implementation", libs.lib("compose-ui-tooling-preview"))

    // Previews are required at three ramp points for every screen, per section
    // 6.4 of scaffolding.md, so the tooling artifact ships in debug builds.
    add("debugImplementation", libs.lib("compose-ui-tooling"))
    add("debugImplementation", libs.lib("compose-ui-test-manifest"))

    add("androidTestImplementation", libs.lib("compose-ui-test-junit4"))
}
