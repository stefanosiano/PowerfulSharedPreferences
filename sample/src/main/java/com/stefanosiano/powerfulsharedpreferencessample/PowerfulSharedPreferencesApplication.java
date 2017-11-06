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
        Prefs.init(this, "shared_prefs_file_name", Context.MODE_PRIVATE);
        Prefs.setDefaultCrypter("password", null);
    }
}
