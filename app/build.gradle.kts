plugins {
    id("horizon.android.application")
    id("horizon.android.compose")
}

android {
    namespace = "dev.horizon.filter"

    defaultConfig {
        applicationId = "dev.horizon.filter"
        versionCode = 2
        versionName = "1.0.0-rc1"
    }
}

dependencies {
    implementation(libs.compose.material3)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.uiautomator)
    implementation(project(":core:model"))
    implementation(project(":core:common"))
    implementation(project(":core:tokens"))
    implementation(project(":core:design"))
    implementation(project(":core:data"))
    implementation(project(":core:solar"))

    implementation(project(":feature:home"))
    implementation(project(":feature:schedule"))
    implementation(project(":feature:presets"))
    implementation(project(":feature:settings"))
    implementation(project(":feature:permission"))
    implementation(project(":feature:mission"))

    implementation(project(":surface:overlay"))
    implementation(project(":surface:notification"))
    implementation(project(":surface:tile"))
    implementation(project(":surface:widget"))
}
