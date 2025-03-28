buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:8.3.0") // или твоя версия AGP
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.22") // или твоя версия Kotlin
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}
