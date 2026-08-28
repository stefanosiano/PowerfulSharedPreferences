plugins {
    id(Deps.androidLibrary)
    id(Deps.detektPlugin)
}

ext {
    set("LIB_VERSION", "1.2.0") // This is the library version used when deploying the artifact
    set("ENABLE_DEPLOY", "true") //Flag whether the ci/cd workflow should deploy to sonatype or not

    set("LIB_GROUP_ID", "io.github.stefanosiano.powerful_libraries")                              // Maven Group ID for the artifact
    set("LIB_ARTIFACT_ID", "sharedpreferences_livedata")                                                        // Maven Artifact ID for the artifact
    set("LIB_NAME", "Powerful SharedPreferences Live Data")                                                        // Library name
    set("SITE_URL", "https://github.com/stefanosiano/PowerfulSharedPreferences")                          // Homepage URL of the library
    set("GIT_URL", "https://github.com/stefanosiano/PowerfulSharedPreferences.git")                       // Git repository URL
    set("LIB_DESCRIPTION", "Adds Live Data support to PowerfulSharedPreferences") // Library description

}

android {
    namespace = "com.stefanosiano.powerful_libraries.sharedpreferences_livedata"
    compileSdk = Deps.sdkCompile

    defaultConfig {
        minSdk = Deps.sdkMin
        consumerProguardFiles("psp-proguard-rules.txt")
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {
    implementation(Deps.kotlinCoroutinesCore)
    implementation(Deps.lifecycleViewModel)
    implementation(Deps.lifecycleLiveData)
    implementation(Deps.powerfulSharedPreferences)
    detektPlugins(Deps.detektKtlintDependency)
}

apply("${rootProject.projectDir}/sonatype-publish.gradle")

detekt {
    toolVersion = Deps.detektPluginVersion
    config.setFrom("${rootDir}/config/detekt/detekt.yml")
//    allRules = true
    buildUponDefaultConfig = true
    autoCorrect = false
}
