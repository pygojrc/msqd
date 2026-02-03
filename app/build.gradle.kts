import org.jetbrains.kotlin.ir.backend.js.compile

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "ms.qd"
    compileSdk = 36

    defaultConfig {
        applicationId = "ms.qd"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildToolsVersion = "36.1.0"
    ndkVersion = "29.0.14033849 rc4"
}

dependencies {
    // Source: https://mvnrepository.com/artifact/org.java-websocket/Java-WebSocket
    implementation("org.java-websocket:Java-WebSocket:1.6.0")

    implementation(libs.androidx.core.ktx)
//    compileOnly(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(files("..\\lib\\gson.jar"))
    // Source: https://mvnrepository.com/artifact/com.google.code.gson/gson
//    implementation(libs.gson)
    compileOnly(files("..\\lib\\qd_443.jar"))
//    testImplementation(libs.junit)
//    androidTestImplementation(libs.androidx.junit)
//    androidTestImplementation(libs.androidx.espresso.core)
}