plugins {
    id("horizon.android.library")
}

android {
    namespace = "dev.horizon.surface.notification"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:common"))
    implementation(project(":core:tokens"))
    implementation(project(":core:data"))
}
