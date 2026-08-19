import com.android.build.api.dsl.ApplicationExtension
import dev.horizon.buildlogic.libs
import dev.horizon.buildlogic.version
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.application")
    id("horizon.quality")
    id("horizon.checks")
    id("horizon.app.checks")
}

configure<ApplicationExtension> {
    compileSdk = libs.version("compileSdk").toInt()

    defaultConfig {
        minSdk = libs.version("minSdk").toInt()
        targetSdk = libs.version("targetSdk").toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    lint {
        abortOnError = true
        warningsAsErrors = true
        checkDependencies = true
        lintConfig = rootProject.layout.projectDirectory.file("config/lint/lint.xml").asFile
    }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
