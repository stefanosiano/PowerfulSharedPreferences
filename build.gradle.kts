// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id(Deps.detektPlugin).version(Deps.detektPluginVersion)
}
buildscript {

    repositories {
        google()
    }
    dependencies {
        classpath(Deps.androidGradlePlugin)
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

detekt {
    toolVersion = Deps.detektPluginVersion
    config.setFrom("config/detekt/detekt.yml")
    buildUponDefaultConfig = true
}
