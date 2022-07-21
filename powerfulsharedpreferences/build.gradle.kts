plugins {
    id(Deps.androidLibrary)
    id(Deps.detektPlugin)
    kotlin("android")
}

ext {
    set("LIB_VERSION", "1.0.19") // This is the library version used when deploying the artifact
    set("ENABLE_DEPLOY", "true") //Flag whether the ci/cd workflow should deploy to sonatype or not

    set("LIB_GROUP_ID", "io.github.stefanosiano.powerful_libraries")                              // Maven Group ID for the artifact
    set("LIB_ARTIFACT_ID", "sharedpreferences")                                                        // Maven Artifact ID for the artifact
    set("LIB_NAME", "Powerful SharedPreferences")                                                        // Library name
    set("SITE_URL", "https://github.com/stefanosiano/PowerfulSharedPreferences")                          // Homepage URL of the library
    set("GIT_URL", "https://github.com/stefanosiano/PowerfulSharedPreferences.git")                       // Git repository URL
    set("LIB_DESCRIPTION", "Android library with a powerful and easy SharedPreferences wrapper, with support for obfuscation, logs, multiple SharedPreferences files, type safety and callback on changes (optionally with Android LiveData).") // Library description

}

android {
    compileSdk = 32

    defaultConfig {
        minSdk = 14
        targetSdk = 32
        consumerProguardFiles("psp-proguard-rules.txt")
    }
}

dependencies {
    implementation(Deps.kotlinStdLib)
    implementation(Deps.kotlinCoroutinesCore)

    testImplementation(Deps.kotlinTestJunit)
    testImplementation(Deps.robolectric)
    testImplementation(Deps.androidxCore)
    testImplementation(Deps.androidxRunner)
    testImplementation(Deps.androidxTestCoreKtx)
    testImplementation(Deps.androidxTestRules)
    testImplementation(Deps.androidxJunit)
    testImplementation(Deps.androidxCoreKtx)
    testImplementation(Deps.mockitoKotlin)

    detektPlugins(Deps.detektKtlintDependency)
}

apply("${rootProject.projectDir}/sonatype-publish.gradle")

detekt {
    toolVersion = Deps.detektPluginVersion
    config = files("${rootDir}/config/detekt/detekt.yml")
//    allRules = true
    buildUponDefaultConfig = true
    autoCorrect = false
}
