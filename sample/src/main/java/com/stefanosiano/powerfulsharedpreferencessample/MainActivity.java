package com.stefanosiano.powerfulsharedpreferencessample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.stefanosiano.powerfulsharedpreferences.PowerfulPreference;
import com.stefanosiano.powerfulsharedpreferences.Prefs;

import java.math.BigDecimal;

public class MainActivity extends Activity {

    public static final PowerfulPreference<Integer> preference1 = Prefs.INSTANCE.newPref("p3", 1);
    public static final PowerfulPreference<Double> preference2 = Prefs.INSTANCE.newPref("p4", 1D);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Log.e("ASD2", preference1.get()+"");
        Log.e("ASD2", preference2.get()+"");
        Prefs.INSTANCE.put(preference1, 2);
        Prefs.INSTANCE.put(preference2, 2.54);
        Log.e("ASD2", preference1.get()+"");
        Log.e("ASD2", preference2.get()+"");


        PowerfulPreference<BigDecimal> pref = new PowerfulPreference<BigDecimal>("pref", BigDecimal.ZERO) {
            @Override public BigDecimal parse(String s) {return new BigDecimal(s);}
            @Override public Class getPrefClass() {return BigDecimal.class;}
        };
    }
}
