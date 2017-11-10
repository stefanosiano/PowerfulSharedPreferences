package com.stefanosiano.powerfulsharedpreferencessample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.stefanosiano.powerfulsharedpreferences.PowerfulPreference;
import com.stefanosiano.powerfulsharedpreferences.Prefs;

public class MainActivity extends Activity {

    public static final PowerfulPreference<Integer> preference1 = Prefs.newPref("p3", 1);
    public static final PowerfulPreference<Double> preference2 = Prefs.newPref("p4", 1D);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Prefs.put("a", 142);
        Prefs.put("b", 42.9F);

        Log.e("ASD", Prefs.get("a", 0)+"");

        Log.e("ASD", Prefs.get("b", 0)+"");
        Log.e("ASD", Prefs.get("b2", 0)+"");


        Log.e("ASD2", Prefs.get(preference1)+"");
        Log.e("ASD2", Prefs.get(preference2)+"");
        Prefs.put(preference1, 2);
        Prefs.put(preference2, 2.54);
        Log.e("ASD2", Prefs.get(preference1)+"");
        Log.e("ASD2", Prefs.get(preference2)+"");
    }
}
