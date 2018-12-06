package com.stefanosiano.powerfullibraries.sharedpreferencessample

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.stefanosiano.powerfullibraries.sharedpreferences.PowerfulPreference
import com.stefanosiano.powerfullibraries.sharedpreferences.Prefs
import java.math.BigDecimal


var preference1 by Prefs.newPref("p3", 1)
val preference2 = Prefs.newPref("p4", 1.0)
var preference3 by Prefs.newEnumPref(MyEnum::class.java, "p4", MyEnum.enum1)

class MainActivityKotlin : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        preference3 = MyEnum.enum2

        Log.e("ASD2", preference1.toString())
        Log.e("ASD2", preference2.get().toString())
        preference1 = 2
        Prefs.put(preference2, 2.54)
        preference2.put(3.12)
        Log.e("ASD2", preference1.toString())
        Log.e("ASD2", preference2.get().toString())


        val pref by object : PowerfulPreference<BigDecimal>("pref", BigDecimal.ZERO) {
            override fun getPrefClass() = BigDecimal::class.java
            override fun parse(s: String) = BigDecimal(s)
        }
    }

}

enum class MyEnum {
    enum1, enum2
}