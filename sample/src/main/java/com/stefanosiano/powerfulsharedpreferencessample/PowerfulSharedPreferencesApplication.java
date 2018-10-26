package com.stefanosiano.powerfulsharedpreferencessample;

import android.app.Application;
import android.content.Context;

import com.stefanosiano.powerfulsharedpreferences.Prefs;

/**
 * Application class
 */

public class PowerfulSharedPreferencesApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Prefs.INSTANCE.init(this)
                .setLogLevel(BuildConfig.DEBUG ? Prefs.Builder.Companion.getLOG_VERBOSE() : Prefs.Builder.Companion.getLOG_DISABLED())
                .setDefaultPrefs("shared_prefs_file_name", Context.MODE_PRIVATE)
                .addPrefs("unencrypted_shared_prefs_file_name2", Context.MODE_PRIVATE, false)
                .addPrefs("encrypted_shared_prefs_file_name2", Context.MODE_PRIVATE, true)
                .setCrypter("password", null)
                .build();
    }

}
