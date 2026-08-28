object Deps {

    // Sdk versions
    const val sdkCompile = 35
    const val sdkTarget = sdkCompile
    const val sdkMin = 21

    private const val agpVersion = "9.3.2"
    private const val coroutinesVersion = "1.9.0"
    const val detektPluginVersion = "1.23.7"

    // Gradle plugins
    const val androidGradlePlugin = "com.android.tools.build:gradle:$agpVersion"
    const val detektPlugin = "io.gitlab.arturbosch.detekt"
    const val androidLibrary = "com.android.library"
    const val androidApplication = "com.android.application"

    // Kotlin things
    const val kotlinCoroutinesCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion"

    // Android things
    const val appCompat = "androidx.appcompat:appcompat:1.7.0"
    const val constraintLayout = "androidx.constraintlayout:constraintlayout:2.2.1"
    const val lifecycleViewModel = "androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7"
    const val lifecycleLiveData = "androidx.lifecycle:lifecycle-livedata-ktx:2.8.7"

    // Other libraries
    const val detektKtlintDependency = "io.gitlab.arturbosch.detekt:detekt-formatting:$detektPluginVersion"
    const val powerfulSharedPreferences = "io.github.stefanosiano.powerful_libraries:sharedpreferences:1.2.0"

    // Test libraries
    private const val androidxTestVersion = "1.6.1"
    const val kotlinTestJunit = "org.jetbrains.kotlin:kotlin-test-junit:2.1.0"
    const val robolectric = "org.robolectric:robolectric:4.14.1"
    const val androidxCore = "androidx.test:core:$androidxTestVersion"
    const val androidxRunner = "androidx.test:runner:$androidxTestVersion"
    const val androidxTestCoreKtx = "androidx.test:core-ktx:$androidxTestVersion"
    const val androidxTestRules = "androidx.test:rules:$androidxTestVersion"
    const val androidxJunit = "androidx.test.ext:junit:1.2.1"
    const val androidxCoreKtx = "androidx.core:core-ktx:1.15.0"
    const val mockitoKotlin = "org.mockito.kotlin:mockito-kotlin:5.4.0"
}
