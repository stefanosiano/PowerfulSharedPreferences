plugins {
    id(Deps.androidApplication)
    kotlin("android")
}

android {
    compileSdk = 32

    defaultConfig {
        applicationId = "com.stefanosiano.powerfullibraries.sharedpreferences"
        minSdk = 14
        targetSdk = 32
        versionCode = 1
        versionName = "1.0"

    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
        debug {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(project(":powerfulsharedpreferences"))
    implementation(Deps.constraintLayout)
    implementation(Deps.kotlinStdLib)
    implementation(Deps.kotlinCoroutinesCore)
}
