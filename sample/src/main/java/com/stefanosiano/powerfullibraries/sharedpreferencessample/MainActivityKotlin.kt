package com.stefanosiano.powerfullibraries.sharedpreferencessample

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.stefanosiano.powerfullibraries.sharedpreferences.PowerfulPreference
import com.stefanosiano.powerfullibraries.sharedpreferences.Prefs
import java.math.BigDecimal


val preference1 = Prefs.newPref("p3", 1)
val preference2 = Prefs.newPref("p4", 1.0)

class MainActivityKotlin : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        Log.e("ASD2", preference1.get().toString() + "")
        Log.e("ASD2", preference2.get().toString() + "")
        Prefs.put(preference1, 2)
        Prefs.put(preference2, 2.54)
        Log.e("ASD2", preference1.get().toString() + "")
        Log.e("ASD2", preference2.get().toString() + "")


        val pref = object : PowerfulPreference<BigDecimal>("pref", BigDecimal.ZERO) {
            override fun getPrefClass() = BigDecimal::class.java
            override fun parse(s: String) = BigDecimal(s)
        }
    }

}
