pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io") // если будешь подключать сторонние библиотеки
    }
}

rootProject.name = "Navigation"
include(":app")
