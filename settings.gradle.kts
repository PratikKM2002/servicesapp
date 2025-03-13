pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    plugins {
        // Declare which version of the Android Gradle Plugin and Kotlin plugin to use
        id("com.android.application") version "8.0.2"
        id("org.jetbrains.kotlin.android") version "1.8.21"
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "serviceapp"
include(":app")
