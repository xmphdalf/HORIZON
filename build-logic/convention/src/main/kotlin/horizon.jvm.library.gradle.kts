import dev.horizon.buildlogic.lib
import dev.horizon.buildlogic.libs
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("horizon.quality")
    id("horizon.checks")
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    add("testImplementation", libs.lib("junit-jupiter"))
    add("testRuntimeOnly", libs.lib("junit-platform-launcher"))
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
