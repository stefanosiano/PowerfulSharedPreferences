package com.stefanosiano.powerful_libraries.sharedpreferencessample

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.stefanosiano.powerful_libraries.sharedpreferences.PowerfulPreference
import com.stefanosiano.powerful_libraries.sharedpreferences.Prefs
import java.math.BigDecimal


var preference1 by Prefs.newPref("p1", 1)
val preference2 = Prefs.newPref("p2", 1.0)
var preference3 by Prefs.newEnumPref(MyEnum::class.java, "p3", MyEnum.Enum1)

class MainActivityKotlin : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        preference3 = MyEnum.Enum2

        Log.e("ASD2", preference1.toString())
        Log.e("ASD2", preference2.get().toString())
        preference1 = 2
        Prefs.put(preference2, 2.54)
        Prefs.put("p1", 9)
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
    Enum1, Enum2
}