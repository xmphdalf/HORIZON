plugins {
    id("horizon.android.library")
    id("horizon.android.compose")
}

android {
    namespace = "dev.horizon.feature.mission"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:common"))
    implementation(project(":core:tokens"))
    implementation(project(":core:design"))
    implementation(project(":core:data"))
}
