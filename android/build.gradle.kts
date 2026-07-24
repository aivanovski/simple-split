plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.parcelize) apply false
    alias(libs.plugins.kapt) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.openapiGenerator) apply false
}

subprojects {
    repositories {
        google()
        mavenCentral()
    }
}
