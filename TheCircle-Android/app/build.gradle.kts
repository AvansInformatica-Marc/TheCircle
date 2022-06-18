@file:android.annotation.SuppressLint("FragmentGradleConfiguration")

import org.jetbrains.kotlin.konan.properties.Properties
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toUpperCaseAsciiOnly

plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.devtools.ksp") version "1.6.21-1.0.6"
    id("kotlin-parcelize")

    // Firebase crashlytics
    // id("com.google.gms.google-services")
    // id("com.google.firebase.crashlytics")

    // Navigation
    id("androidx.navigation.safeargs.kotlin")

    // API
    kotlin("plugin.serialization")
}

fun getLocalProperties(): Properties {
    return Properties().also { properties ->
        try {
            file("../local.properties").inputStream().use {
                properties.load(it)
            }
        } catch (ignored: java.io.FileNotFoundException) {}
    }
}

android {
    compileSdk = 32
    buildToolsVersion = "33.0.0"
    namespace = "nl.marc.thecircle"

    packagingOptions {
        resources {
            excludes += "kotlin/**"
            excludes += "**/*.kotlin_metadata"
            excludes += "DebugProbesKt.bin"
            excludes += "META-INF/*.kotlin_module"
            excludes += "META-INF/*.version"
            excludes += "build-data.properties"
        }
    }

    val keys = getLocalProperties()

    fun getProperty(key: String): String? {
        return keys.getProperty(key) ?: System.getenv(key.toUpperCaseAsciiOnly().replace(".", "_"))
    }

    defaultConfig {
        applicationId = "nl.marc.thecircle"
        minSdk = 26
        targetSdk = 32
        versionCode = getProperty("version.code")?.toInt() ?: 1
        versionName = getProperty("version.name") ?: "0.1"

        testBuildType = "debug"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "THE_CIRCLE_HOST", "\"${getProperty("theCircle.host")}\"")
        buildConfigField("int", "THE_CIRCLE_PORT", getProperty("theCircle.port") ?: "80")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            isShrinkResources = false
            isCrunchPngs = false

            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }

        getByName("debug") {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-preview"

            isMinifyEnabled = false
            isShrinkResources = false
            isCrunchPngs = false

            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    bundle {
        abi.enableSplit = true
        language.enableSplit = true
        density.enableSplit = true
        texture.enableSplit = true
        deviceTier.enableSplit = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }

    buildFeatures {
        viewBinding = true
        // compose = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    // Firebase crashlytics
    // implementation(platform("com.google.firebase:firebase-bom:29.0.2"))
    // implementation("com.google.firebase:firebase-crashlytics-ktx")

    // Navigation
    val androidxNavigationVersion = rootProject.extra["androidxNavigationVersion"]
    implementation("androidx.navigation:navigation-fragment-ktx:${androidxNavigationVersion}")
    implementation("androidx.navigation:navigation-ui-ktx:$androidxNavigationVersion")
    testImplementation("androidx.navigation:navigation-testing:$androidxNavigationVersion")

    // API
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:0.8.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")

    // Networking
    implementation("io.ktor:ktor-network:2.0.2")
    implementation("io.ktor:ktor-network-tls:2.0.2")

    // Streaming
    implementation("com.github.pedroSG94.rtmp-rtsp-stream-client-java:rtplibrary:2.1.9")

    // Database
    val roomVersion = "2.4.2"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Dependency Injection
    val koinVersion = "3.2.0"
    implementation("io.insert-koin:koin-android:$koinVersion")
    implementation("io.insert-koin:koin-androidx-navigation:$koinVersion")
    // testImplementation("io.insert-koin:koin-test-junit5:$koinVersion")
    // androidTestImplementation("io.insert-koin:koin-test-junit5:$koinVersion")

    // Backward compatibility & utilities
    implementation("androidx.core:core-ktx:1.8.0")

    implementation("androidx.appcompat:appcompat:1.4.2")

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.1")
    implementation("androidx.fragment:fragment-ktx:1.4.1")
    // TODO: Change to testImplementation when https://issuetracker.google.com/issues/127986458 is fixed
    debugImplementation("androidx.fragment:fragment-testing:1.4.1")
    testReleaseImplementation("androidx.fragment:fragment-testing:1.4.1")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.2")

    // Design
    implementation("com.google.android.material:material:1.6.1")

    // Test base
    val mockkVersion = "1.12.4"
    testImplementation(kotlin("test-junit"))
    testImplementation("org.robolectric:robolectric:4.8.1")
    testImplementation("io.mockk:mockk:${mockkVersion}")
    testImplementation("io.mockk:mockk-agent-jvm:${mockkVersion}")
    testImplementation("androidx.test:runner:1.4.0")
    testImplementation("androidx.test.ext:junit:1.1.3")

    // Android test base
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    androidTestImplementation("androidx.test:runner:1.4.0")
    androidTestImplementation(kotlin("test-junit"))
}
