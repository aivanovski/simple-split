plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.parcelize)
    alias(libs.plugins.kapt)
    alias(libs.plugins.kotlinSerialization)
}

fun getVersionName(): String = libs.versions.appVersion.get()

fun getVersionCode(): Int {
    val values = getVersionName().split(".")
    return values[0].toInt() * 10000 + values[1].toInt() * 100 + values[2].toInt()
}

android {
    namespace = "com.github.ai.simplesplit.android"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.github.ai.simplesplit.android"
        minSdk = 26
        targetSdk = 34
        versionCode = getVersionCode()
        versionName = getVersionName()
        multiDexEnabled = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }

    buildFeatures {
        viewBinding = true
        compose = true
        buildConfig = true
    }

    kapt {
        arguments {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }

    packaging {
        resources {
            excludes.addAll(
                listOf(
                    "/META-INF/{AL2.0,LGPL2.1}",
                    "META-INF/INDEX.LIST",
                    "META-INF/io.netty.versions.properties"
                )
            )
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
        }

        release {
            isMinifyEnabled = false

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    testImplementation(libs.junit4)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.truth)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Android
    implementation(libs.androidx.multidex)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.vm)
    implementation(libs.androidx.lifecycle.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.viewMaterial)
    implementation(libs.androidx.icons)

    // Timber
    implementation(libs.timber)

    // Koin
    implementation(libs.koin)
    implementation(libs.koin.android)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.kts)
    kapt(libs.room.compiler)
    androidTestImplementation(libs.room.test)

    // Network
    implementation(libs.ktorClientCore)
    implementation(libs.ktorClientOkhttp)
    implementation(libs.ktorClientLogging)
    implementation(libs.ktorClientNegotiation)
    implementation(libs.ktorSerializationJson)

    // Json
    implementation(libs.kotlinx.json)

    // Arrow
    implementation(libs.arrow.core)
    implementation(libs.arrow.coroutines)

    // Navigation
    implementation(libs.decompose)
    implementation(libs.decompose.extensions)

    // Api
    implementation(project(":backend-api"))

    // Preferences
    implementation(libs.ksprefs)
}