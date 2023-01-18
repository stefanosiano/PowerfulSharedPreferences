package com.stefanosiano.powerful_libraries.sharedpreferencessample

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.stefanosiano.powerful_libraries.sharedpreferences.Prefs
import java.math.BigDecimal

/** Simple Activity. */
class MainActivityKotlin : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Preferences.preference3 = MyEnum.Enum2

        Log.e("ASD2", Preferences.preference1.toString())
        Preferences.preference1 = 2
        Log.e("ASD2", Preferences.preference1.toString())
        Preferences.preference1 = 1
        Log.e("ASD2", Preferences.preference1.toString())
        Log.e("ASD2", "aaaaaaaaaaaaaaaa")
        Log.e("ASD2", Preferences.preference2.get().toString())
        Preferences.preference1 = 2
        Prefs.put(Preferences.preference2, 1.0)
        Prefs.put("p1", 1)
        Preferences.preference2.put(2.0)
        Log.e("ASD2", Preferences.preference1.toString())
        Log.e("ASD2", Preferences.preference2.get().toString())
        Preferences.prefBd = BigDecimal.ONE
        Log.e("ASD2", Preferences.prefBd.toString())
        Preferences.prefBd2 = BigDecimal.ONE
        Log.e("ASD2", Preferences.prefBd2.toString())
        Preferences.prefMc3 = MyClass("new value")
        Log.e("ASD2", Preferences.prefMc3.toString())
    }
}
