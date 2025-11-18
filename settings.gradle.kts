pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // You can add other repositories like jitpack.io here if needed
        // maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "storeit"
include(":app")
