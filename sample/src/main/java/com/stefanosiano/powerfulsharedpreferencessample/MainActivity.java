package com.stefanosiano.powerfulsharedpreferencessample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.stefanosiano.powerfulsharedpreferences.PowerfulPreference;
import com.stefanosiano.powerfulsharedpreferences.Prefs;

public class MainActivity extends Activity {

    public static final PowerfulPreference<Integer> preference1 = Prefs.newPref("", 1);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Prefs.putLong("a", 142);
        Prefs.putFloat("b", 42.9F);

        Log.e("ASD", Prefs.get("a", 0)+"");
        Log.e("ASD", Prefs.get("b", 0)+"");
        Log.e("ASD", Prefs.get("b2", 0)+"");


        Integer s = Prefs.get(preference1);

    }
}
