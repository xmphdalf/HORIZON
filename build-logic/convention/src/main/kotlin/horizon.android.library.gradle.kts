import com.android.build.api.dsl.LibraryExtension
import dev.horizon.buildlogic.lib
import dev.horizon.buildlogic.libs
import dev.horizon.buildlogic.version
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.library")
    id("horizon.quality")
    id("horizon.checks")
}

configure<LibraryExtension> {
    compileSdk = libs.version("compileSdk").toInt()

    defaultConfig {
        minSdk = libs.version("minSdk").toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    lint {
        abortOnError = true
        warningsAsErrors = true
        checkDependencies = true
        lintConfig = rootProject.layout.projectDirectory.file("config/lint/lint.xml").asFile
    }
}

// AGP 9 carries Kotlin built in, so org.jetbrains.kotlin.android is never
// applied. Configuring the compile tasks directly avoids depending on which
// Kotlin extension type AGP registers for Android variants.
tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    add("testImplementation", libs.lib("junit-jupiter"))
    add("testRuntimeOnly", libs.lib("junit-platform-launcher"))
    add("androidTestImplementation", libs.lib("androidx-test-junit"))
    add("androidTestImplementation", libs.lib("androidx-test-runner"))
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
