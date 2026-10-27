plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}
android {
    namespace = "com.deploydulupulangnanti.rtsmartpro"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.deploydulupulangnanti.rtsmartpro"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "API_BASE_URL", "\"https://rtsmartpro.amarlo.online/api/v1/\"")
    }
    signingConfigs {
        create("release") {
            val signingFile = providers.environmentVariable("RTSMART_KEYSTORE").orNull
            if (signingFile != null) {
                storeFile = file(signingFile)
                storePassword = providers.environmentVariable("RTSMART_STORE_PASSWORD").orNull
                keyAlias = "rtsmartpro"
                keyPassword = providers.environmentVariable("RTSMART_KEY_PASSWORD").orNull
            }
        }
    }
    buildTypes {
        release {
            val optimize = providers.gradleProperty("optimizeRelease").orNull == "true"
            isMinifyEnabled = optimize
            isShrinkResources = optimize
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    buildFeatures { compose = true; buildConfig = true }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
}
dependencies {
    implementation(platform("androidx.compose:compose-bom:2025.09.01"))
    implementation("androidx.activity:activity-compose:1.11.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    debugImplementation("androidx.compose.ui:ui-tooling")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test:runner:1.7.0")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.3.0")
}
