plugins {
    id(Deps.androidApplication)
    id(Deps.detektPlugin)
}

android {
    namespace = "com.stefanosiano.powerful_libraries.sharedpreferencessample"
    compileSdk = Deps.sdkCompile

    buildFeatures {
        buildConfig = true
    }

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
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    implementation(project(":powerfulsharedpreferences"))
    implementation(Deps.constraintLayout)
    implementation(Deps.kotlinCoroutinesCore)

    detektPlugins(Deps.detektKtlintDependency)
}

detekt {
    toolVersion = Deps.detektPluginVersion
    config.setFrom("${rootDir}/config/detekt/detekt.yml")
//    allRules = true
    buildUponDefaultConfig = true
    autoCorrect = false
}
