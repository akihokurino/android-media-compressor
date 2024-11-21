plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.compose)

  kotlin("kapt")
  id("com.google.dagger.hilt.android")
  id("org.jetbrains.kotlin.plugin.serialization")
  id("com.google.devtools.ksp")
}

android {
  namespace = "app.akiho.media_compressor"
  compileSdk = 35

  defaultConfig {
    applicationId = "app.akiho.media_compressor"
    minSdk = 31
    targetSdk = 35
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
  composeOptions { kotlinCompilerExtensionVersion = "1.5.1" }
  packaging {
    resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" }
    pickFirst("lib/arm64-v8a/libc++_shared.so")
    pickFirst("lib/x86_64/libc++_shared.so")
    pickFirst("lib/x86/libc++_shared.so")
    pickFirst("lib/armeabi-v7a/libc++_shared.so")
  }
}

dependencies {
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.ui)
  implementation(libs.androidx.ui.graphics)
  implementation(libs.androidx.ui.tooling.preview)
  implementation(libs.androidx.material3)
  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.ui.test.junit4)
  debugImplementation(libs.androidx.ui.tooling)
  debugImplementation(libs.androidx.ui.test.manifest)
  implementation(libs.androidx.media3.exoplayer)
  implementation(libs.androidx.exifinterface)
  implementation(libs.androidx.media3.ui)

  // hilt
  implementation(libs.hilt.android)
  kapt(libs.hilt.compiler)
  implementation(libs.androidx.hilt.navigation.compose)
  // coil
  implementation(libs.coil.compose)
  implementation(libs.coil.svg)
  implementation(libs.coil.video)
  // okhttp
  implementation(libs.okhttp)
  // ffmpeg
  implementation(libs.ffmpeg.kit.full.gpl)
}

kapt { correctErrorTypes = true }