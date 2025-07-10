pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "simple-split-android"

include(
    ":app",
    ":backend-api"
)