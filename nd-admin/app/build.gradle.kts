plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.sd.ndadmin"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.sd.ndadmin"
        minSdk = 26
        targetSdk = 36
        versionCode = 15
        versionName = "2.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file("/Users/sriram/Desktop/yard/rest/bin/com.sd.ndadmin-keystore/nd.keystore")
            storePassword = "109yoga"
            keyAlias = "ndkey"
            keyPassword = "N1thyaDharm@"
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }
}

dependencies {
    // Core & Activity/Fragment
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Jetpack Compose & Material 3
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.ui)
    implementation(libs.material3)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.ui.graphics)
    implementation(libs.androidx.navigation.compose)

    // DataStore (Preferences)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore.core.android)

    // Room Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.common.jvm)
    ksp(libs.androidx.room.compiler)

    // Firebase (FCM & Analytics)
    implementation(platform(libs.firebaseBom))
    implementation(libs.firebase.messaging.ktx)
    implementation(libs.firebaseFirestore)
    implementation(libs.firebaseAuth)
    implementation(libs.firebaseAnalytics)

    // GitHub REST API & Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.gson)

    // Image Loading (Coil)
    implementation(libs.coil.compose)

    // Location & Play Services
    implementation(libs.play.services.auth)
    implementation(libs.play.services.location)

    // Additional Utilities
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.opencsv)
    implementation(libs.work.runtime.ktx)
    implementation(libs.osmbonuspack)
    implementation(libs.androidx.vectordrawable)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
//plugins {
//    alias(libs.plugins.android.application)
//    alias(libs.plugins.kotlin.android)
//    alias(libs.plugins.kotlin.compose)
//    alias(libs.plugins.google.services)
//    alias(libs.plugins.kotlinSerialization)
//    alias(libs.plugins.ksp)
//}
//
//android {
//    namespace = "com.sd.ndadmin"
//    compileSdk = 36 // Update compileSdk to match targetSdk
//
//    signingConfigs {
//        create("release") {
//            storeFile = file("/Users/sriram/Desktop/yard/rest/bin/com.sd.ndadmin-keystore/nd.keystore") // Update with your keystore path
//            storePassword = "109yoga" // Replace with your password
//            keyAlias = "ndkey" // Replace with your alias
//            keyPassword = "N1thyaDharm@" // Replace with your key password
//        }
//    }
//    buildTypes {
//        getByName("release") {
//            signingConfig = signingConfigs.getByName("release")
//            isMinifyEnabled = false // Optional: Enable for size optimization
//            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
//        }
//    }
//    kotlinOptions {
//        jvmTarget = "1.8"
//    }
//    buildFeatures {
//        compose = true
//    }
//
//    composeOptions {
//        kotlinCompilerExtensionVersion = "1.5.4" // Or use the latest
//    }
//
//    defaultConfig {
//        applicationId = "com.sd.ndadmin"
//        minSdk = 26
//        targetSdk = 36
//        versionCode = 15 // this is required to push the app, keep increasing the number every push to store
//        versionName = "2.0"
//        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
//    }
//
//    buildTypes {
//        release {
//            isMinifyEnabled = false
//            proguardFiles(
//                getDefaultProguardFile("proguard-android-optimize.txt"),
//                "proguard-rules.pro"
//            )
//        }
//    }
//    compileOptions {
//        sourceCompatibility = JavaVersion.VERSION_11
//        targetCompatibility = JavaVersion.VERSION_11
//    }
//    kotlinOptions {
//        jvmTarget = "11"
//    }
//    buildFeatures {
//        compose = true
//    }
//}
//
//dependencies {
//
//    implementation(libs.androidx.fragment)
//
//    // Compose & Material3
//    implementation(libs.ui)
//    implementation(libs.material3)
//    implementation(libs.androidx.navigation.compose)
//
////    implementation(libs.swisseph)
//    implementation(libs.kotlinx.serialization.json)
//
//    // Room Database dependencies
//    implementation(libs.androidx.room.runtime)
//    implementation(libs.androidx.room.ktx)
//    implementation(libs.androidx.animation.core.android)
//    implementation(libs.androidx.foundation.layout.android)
//    implementation(libs.androidx.foundation.layout.android)
//    implementation(files("libs/swisseph-2.01.00-02.jar"))
//
//    // Annotation processor for Room
//    ksp(libs.androidx.room.compiler)
//
//    // DataStore
//    implementation(libs.androidx.datastore.preferences)
//
//    // Add this for CSV parsing
//    implementation (libs.opencsv)
//    implementation(libs.work.runtime.ktx)
//
//    implementation(libs.play.services.auth)
//
//    // OSMBonusPack
//    implementation(libs.osmbonuspack)
//
//    implementation(libs.androidx.core.ktx)
//    implementation(libs.androidx.lifecycle.runtime.ktx)
//    implementation(libs.androidx.activity.compose)
//    implementation(platform(libs.androidx.compose.bom))
//    implementation(libs.androidx.ui)
//    implementation(libs.androidx.ui.graphics)
//    implementation(libs.androidx.ui.tooling.preview)
//    implementation(libs.androidx.material3)
//    implementation(libs.androidx.datastore.core.android)
//    implementation(libs.androidx.work.runtime.ktx)
//    implementation(libs.androidx.foundation.layout.android)
//
//    implementation(libs.navigation.compose)
//
//    // Import the Firebase BoM first! This manages the versions for Firebase libraries.
//    implementation(platform(libs.firebaseBom))
//
//    implementation(libs.firebase.messaging.ktx) // Use your messaging alias
//
//    // Now, declare the dependencies for the Firebase products you need
//    // You don't specify versions here because the BoM handles it
//    implementation(libs.firebaseFirestore)
//    implementation(libs.firebaseAuth)
//    implementation(libs.firebaseAnalytics) // If you need Analytics
////    implementation(libs.firebase.analytics.no.ad)
//
//    implementation(libs.play.services.location)
//    implementation(libs.lifecycle.runtime.ktx)
//    implementation(libs.androidx.foundation.android)
//
//    implementation(libs.androidx.vectordrawable)
//    implementation(libs.androidx.room.common.jvm)
//
//    implementation(libs.compose.material3)
//    implementation(libs.compose.material.icons.extended)
//    implementation(libs.compose.ui)
//    implementation(libs.compose.ui.tooling.preview)
//    implementation(libs.compose.ui.graphics)
//
//    testImplementation(libs.junit)
//    androidTestImplementation(libs.androidx.junit)
//    androidTestImplementation(libs.androidx.espresso.core)
//    androidTestImplementation(platform(libs.androidx.compose.bom))
//    androidTestImplementation(libs.androidx.ui.test.junit4)
//    debugImplementation(libs.androidx.ui.tooling)
//    debugImplementation(libs.androidx.ui.test.manifest)
//}
