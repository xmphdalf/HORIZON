import dev.horizon.buildlogic.lib
import dev.horizon.buildlogic.libs
import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import org.jetbrains.kotlin.gradle.dsl.KotlinBaseExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("io.gitlab.arturbosch.detekt")
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(rootProject.layout.projectDirectory.file("config/detekt/detekt.yml"))
    parallel = true
    basePath = rootProject.projectDir.absolutePath
}

dependencies {
    add("detektPlugins", libs.lib("detekt-formatting"))
}

// The detekt plugin registers a catch-all `detekt` task alongside the per-source
// -set `detektMain` and `detektTest`. Running all three analyses every file
// twice, so the catch-all is disabled and the source-set tasks are the real gate.
tasks.named("detekt").configure {
    enabled = false
}

tasks.withType<Detekt>().configureEach {
    // The build fails on any finding. Section 5 of scaffolding.md.
    ignoreFailures = false
    reports {
        html.required.set(true)
        sarif.required.set(true)
        xml.required.set(false)
        txt.required.set(false)
        md.required.set(false)
    }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        allWarningsAsErrors.set(true)
    }
}

// Explicit API mode is required for core/* only, per the module table in
// section 3 of scaffolding.md. Keying it off the project path rather than an
// opt-in plugin means a core module added later cannot silently skip it.
if (path.startsWith(":core:")) {
    extensions.findByType(KotlinBaseExtension::class.java)?.explicitApi = ExplicitApiMode.Strict
}

// Detekt does not attach to `check` for Android variants on its own, so wire it
// explicitly and keep one entry point: ./gradlew check.
tasks.named("check").configure {
    dependsOn(tasks.withType<Detekt>())
}
