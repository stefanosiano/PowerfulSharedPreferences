package com.stefanosiano.powerfulsharedpreferencessample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.stefanosiano.powerfulsharedpreferences.Prefs;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Prefs.putLong("a", 142);
        Prefs.putFloat("b", 42.9F);

        Log.e("ASD", Prefs.getLong("a", 0)+"");
        Log.e("ASD", Prefs.getFloat("b", 0)+"");
        Log.e("ASD", Prefs.getFloat("b2", 0)+"");
    }
}
