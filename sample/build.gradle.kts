plugins {
    id(Deps.androidApplication)
    id(Deps.detektPlugin)
    kotlin("android")
}

android {
    compileSdk = Deps.sdkCompile

    defaultConfig {
        applicationId = "com.stefanosiano.powerfullibraries.sharedpreferences"
        minSdk = Deps.sdkMin
        targetSdk = Deps.sdkTarget
        versionCode = 1
        versionName = "1.0"

    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            isMinifyEnabled = true
            isShrinkResources = true
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

    detektPlugins(Deps.detektKtlintDependency)
}

detekt {
    toolVersion = Deps.detektPluginVersion
    config = files("${rootDir}/config/detekt/detekt.yml")
//    allRules = true
    buildUponDefaultConfig = true
    autoCorrect = false
}
