import java.util.Properties

val localProperties = Properties().apply{
    val file = rootProject.file("dev.properties")
    if(file.exists()){
        file.inputStream().use { load(it) }
    }
}

fun localSetting(name: String): String {
    localProperties.getProperty(name)?.let { return it }

    val isLocalTaskRequested = gradle.startParameter.taskNames.any {
        it.contains("local", ignoreCase = true)
    }
    if (isLocalTaskRequested) {
        error(
            "Missing $name in dev.properties. " +
                    "See dev.properties.example."
        )
    }

    return ""
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "edu.cs673.yikyak_clone"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "edu.cs673.yikyak_clone"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    flavorDimensions += "backend"

    productFlavors {
        create("local") {
            dimension = "backend"

            buildConfigField(
                "String",
                "SUPABASE_URL",
                "\"${localSetting("supabase.local.url")}\""
            )

            buildConfigField(
                "String",
                "SUPABASE_PUBLISHABLE_KEY",
                "\"${localSetting("supabase.local.publishableKey")}\""
            )
        }

        create("production") {
            dimension = "backend"

            buildConfigField(
                "String",
                "SUPABASE_URL",
                "\"https://elpbevyjrifdobkpbdex.supabase.co\""
            )

            buildConfigField(
                "String",
                "SUPABASE_PUBLISHABLE_KEY",
                "\"sb_publishable_t3M58TkchkbB5L_4DFQF0A_8LRRtFbg\""
            )
        }
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.navigation.compose)

    // Base Dependencies needed for Supabase
    implementation(platform(libs.bom))
    implementation(libs.postgrest.kt)
    implementation(libs.auth.kt)
    implementation(libs.realtime.kt)

    // Ktor Client Engine needed for Kotlin Target for Supabase
    implementation(libs.ktor.client.android)

    // Location Services API for Geotagging
    implementation(libs.play.services.location)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
}