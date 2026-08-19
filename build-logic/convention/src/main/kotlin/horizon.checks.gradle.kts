import dev.horizon.buildlogic.DependencyAuditTask
import dev.horizon.buildlogic.ModuleDirectionTask
import dev.horizon.buildlogic.ModuleDirection
import dev.horizon.buildlogic.NoBangBangTask
import org.gradle.api.artifacts.ProjectDependency

// Structural gates from section 5 and section 8 of scaffolding.md. These run as
// part of `check`, so a violation fails the same command that runs the tests.

val thisPath = path
val allowed = ModuleDirection.allowedFor(thisPath)

val moduleDirection = tasks.register<ModuleDirectionTask>("moduleDirection") {
    group = "verification"
    description = "Fails if this module depends on a module its layer may not reach."
    projectPath.set(thisPath)
    permitted.set(allowed.orEmpty())
    constrained.set(allowed != null)
}

val noBangBang = tasks.register<NoBangBangTask>("noBangBang") {
    group = "verification"
    description = "Fails on the !! operator, which section 6.2 of scaffolding.md forbids."
    sources.from(fileTree("src") { include("**/*.kt") })
}

val dependencyAudit = tasks.register<DependencyAuditTask>("dependencyAudit") {
    group = "verification"
    description = "Fails on any analytics, advertising, crash-reporting, or networking dependency."
    projectPath.set(thisPath)
}

// A precompiled script plugin applies before the module's own `dependencies`
// block is evaluated, so reading configurations here would always find them
// empty. Both of these inputs are therefore populated after evaluation.
afterEvaluate {
    // Test configurations are excluded: a test may reach for a fixture its
    // module would never ship.
    val productionConfigurations = listOf("api", "implementation", "compileOnly", "runtimeOnly")

    val declaredPaths = productionConfigurations
        .mapNotNull { configurations.findByName(it) }
        .flatMap { configuration ->
            configuration.dependencies.filterIsInstance<ProjectDependency>().map { it.path }
        }
        .toSet()

    moduleDirection.configure { declared.set(declaredPaths) }

    dependencyAudit.configure {
        configurations
            .filter { it.isCanBeResolved && it.name.endsWith("RuntimeClasspath") }
            .forEach { rootComponents.add(it.incoming.resolutionResult.rootComponent) }
    }
}

tasks.named("check").configure {
    dependsOn(moduleDirection, noBangBang, dependencyAudit)
}
