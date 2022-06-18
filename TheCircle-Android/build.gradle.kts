// Top-level build file where you can add configuration options common to all sub-projects/modules.
val androidxNavigationVersion by extra("2.4.2")

plugins {
    val kotlinVersion = "1.6.21"

    id("com.android.application") version "7.2.1" apply false
    id("com.android.library") version "7.2.1" apply false
    kotlin("android") version kotlinVersion apply false

    // Firebase crashlytics
    // id("com.google.gms.google-services") version "4.3.10" apply false
    // id("com.google.firebase.crashlytics") version "2.8.1" apply false

    // Navigation
    id("androidx.navigation.safeargs.kotlin") version "2.4.2" apply false

    // Testing
    // id("org.jetbrains.kotlinx.kover") version "0.5.0"
    // id("org.sonarqube") version "3.3"

    // API
    kotlin("plugin.serialization") version kotlinVersion apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
