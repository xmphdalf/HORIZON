plugins {
    id("horizon.android.library")
}

android {
    namespace = "dev.horizon.core.data"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:common"))
}
