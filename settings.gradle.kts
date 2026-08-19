pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "horizon"

include(":app")

include(":core:model")
include(":core:common")
include(":core:tokens")
include(":core:solar")
include(":core:design")
include(":core:data")

include(":feature:home")
include(":feature:schedule")
include(":feature:presets")
include(":feature:settings")
include(":feature:permission")
include(":feature:mission")

include(":surface:overlay")
include(":surface:notification")
include(":surface:tile")
include(":surface:widget")

include(":benchmark")
include(":baselineprofile")
