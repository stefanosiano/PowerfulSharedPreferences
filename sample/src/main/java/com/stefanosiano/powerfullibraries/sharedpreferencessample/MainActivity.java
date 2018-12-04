package com.stefanosiano.powerfullibraries.sharedpreferencessample;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.stefanosiano.powerfullibraries.sharedpreferences.PowerfulPreference;
import com.stefanosiano.powerfullibraries.sharedpreferences.Prefs;

import java.math.BigDecimal;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;

public class MainActivity extends Activity {

    private final Function1<Integer, Unit> function2 = (value) -> {
        Log.e("aaaa", "dccdcdcd");
        return Unit.INSTANCE;
    };

    public final PowerfulPreference<Integer> preference1 = Prefs.INSTANCE.newPref("p3", 1).observe(function2);
    public static final PowerfulPreference<Double> preference2 = Prefs.INSTANCE.newPref("p4", 1D);


    private final Function2<String, Object, Unit> function = (key, value) -> {
        Toast.makeText(this, "asdasd", Toast.LENGTH_SHORT).show();
        return Unit.INSTANCE;
    };

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
        Prefs.INSTANCE.observe(function);

        new Handler().postDelayed(()-> Prefs.INSTANCE.put(preference1, 1), 4000);
        new Handler().postDelayed(()-> Prefs.INSTANCE.put(preference2, 2D), 10000);
    }

    @Override
    protected void onStop() {
        Prefs.INSTANCE.stopObserve(function);
        preference1.stopObserve(function2);
        super.onStop();
    }
}
