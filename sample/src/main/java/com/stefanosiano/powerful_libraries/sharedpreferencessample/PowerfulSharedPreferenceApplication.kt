package com.stefanosiano.powerful_libraries.sharedpreferencessample

import android.app.Application
import com.stefanosiano.powerful_libraries.sharedpreferences.Prefs
import com.stefanosiano.powerful_libraries.sharedpreferences.Prefs.Builder.Companion.LOG_DISABLED
import com.stefanosiano.powerful_libraries.sharedpreferences.Prefs.Builder.Companion.LOG_VERBOSE

/** Application class. */
class PowerfulSharedPreferenceApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Prefs.init(this)
            .setLogLevel(if (BuildConfig.DEBUG) LOG_VERBOSE else LOG_DISABLED)
            .setOnPreferenceSet { powerfulPreference, s, s2, s3, s4, s5 -> }
            .setDefaultPrefs("shared_prefs_file_name", MODE_PRIVATE)
            .addPrefs("unobfuscated_shared_prefs_file_name2", MODE_PRIVATE, false)
            .addPrefs("obfuscated_shared_prefs_file_name2", MODE_PRIVATE, true)
            .setObfuscator("password", null)
            .build()
    }
}
