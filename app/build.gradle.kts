import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.room)
}

// Listen Notes settings live in local.properties (not in git):
//   LISTEN_NOTES_API_KEY=...              key for the production API (release, and debug when the switch is on)
//   LISTEN_NOTES_USE_PRODUCTION=true      optional: debug builds use the production API instead of the mock server
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
val listenNotesApiKey: String = localProperties.getProperty("LISTEN_NOTES_API_KEY", "").trim()
val debugUsesProductionApi: Boolean =
    localProperties.getProperty("LISTEN_NOTES_USE_PRODUCTION", "false").trim().toBoolean()
if (debugUsesProductionApi && listenNotesApiKey.isEmpty()) {
    throw GradleException("LISTEN_NOTES_USE_PRODUCTION=true needs LISTEN_NOTES_API_KEY in local.properties")
}

val listenNotesMockUrl = "https://listen-api-test.listennotes.com/api/v2/"
val listenNotesProductionUrl = "https://listen-api.listennotes.com/api/v2/"

android {
    namespace = "com.propodcast.app"
    // Android 17 platform 37.2 (Compose 1.13 needs 37.1+).
    compileSdk {
        version = release(37) {
            minorApiLevel = 2
        }
    }

    defaultConfig {
        applicationId = "com.propodcast.app"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            // Mock server by default (fake data, no key); production API when LISTEN_NOTES_USE_PRODUCTION=true.
            val debugBaseUrl = if (debugUsesProductionApi) listenNotesProductionUrl else listenNotesMockUrl
            val debugApiKey = if (debugUsesProductionApi) listenNotesApiKey else ""
            buildConfigField("String", "LISTEN_NOTES_BASE_URL", "\"$debugBaseUrl\"")
            buildConfigField("String", "LISTEN_NOTES_API_KEY", "\"$debugApiKey\"")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "LISTEN_NOTES_BASE_URL", "\"$listenNotesProductionUrl\"")
            buildConfigField("String", "LISTEN_NOTES_API_KEY", "\"$listenNotesApiKey\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin {
    jvmToolchain(17)
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material3.adaptive.navigation.suite)
    implementation(libs.androidx.material.icons.core)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.session)
    implementation(libs.kotlinx.coroutines.guava)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.timber)

    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.androidx.paging.testing)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.turbine)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
