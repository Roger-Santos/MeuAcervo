pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("com.android.application") version "8.1.3" apply false
        id("com.android.library") version "8.1.3" apply false
        id("org.jetbrains.kotlin.jvm") version "2.0.0" apply false
        id("org.jetbrains.kotlin.android") version "2.0.0" apply false
        id("org.jetbrains.kotlin.kapt") version "2.0.0" apply false
        // Compose Compiler plugin vem do próprio Kotlin 2.0.0
        id("org.jetbrains.kotlin.plugin.compose") version "2.0.0" apply false
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "MeuAcervo"
include(":app")