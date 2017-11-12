package com.stefanosiano.powerfulsharedpreferencessample;

import android.app.Application;
import android.content.Context;

import com.stefanosiano.powerfulsharedpreferences.Prefs;

/**
 * Created by stefano on 06/11/17.
 */

public class PowerfulSharedPreferencesApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Prefs.init(this)
                .setLogLevel(BuildConfig.DEBUG ? Prefs.Builder.LOG_VERBOSE : Prefs.Builder.LOG_DISABLED)
                .setPrefsName("shared_prefs_file_name", Context.MODE_PRIVATE)
                .setDefaultCrypter("password", null)
                .build();
    }

    @Override
    public void onTerminate() {
        Prefs.terminate();
        super.onTerminate();
    }
}
