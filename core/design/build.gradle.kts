plugins {
    id("horizon.android.library")
    id("horizon.android.compose")
}

android {
    namespace = "dev.horizon.core.design"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:tokens"))
}
