package com.stefanosiano.powerful_libraries.sharedpreferencessample

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.stefanosiano.powerful_libraries.sharedpreferences.PowerfulPreference
import com.stefanosiano.powerful_libraries.sharedpreferences.Prefs
import java.math.BigDecimal


var preference1 by Prefs.newPref("p1", 1)
val preference2 = Prefs.newPref("p2", 1.0)
var preference3 by Prefs.newEnumPref("p3", MyEnum.Enum1)
val preference5: PowerfulPreference<Boolean?> = Prefs.newNullablePref(Boolean::class.java, "p5", null)
val preference6: PowerfulPreference<BigDecimal> = Prefs.newPref(
    "p6",
    BigDecimal.ONE,
    parse = { s -> if (s.isNotEmpty() && s.matches("\\d*\\.?\\d*".toRegex())) BigDecimal(s) else BigDecimal.ONE },
    toPreference = { bd -> bd.toString() }
)
var prefBd by object : PowerfulPreference<BigDecimal>("prefBd", BigDecimal.ZERO) {
    override fun getPrefClass() = BigDecimal::class.java
    override fun parse(s: String) = BigDecimal(s)
}
var prefBd2 by Prefs.newPref("prefBd2", BigDecimal.ZERO, parse = { BigDecimal(it) })

class BigDecimalPreference: PowerfulPreference<BigDecimal> {
    constructor(key: String, defaultValue: BigDecimal, prefName: String): super(key, defaultValue, prefName)
    constructor(key: String, defaultValue: BigDecimal): super(key, defaultValue)
    override fun getPrefClass(): Class<*> = BigDecimal::class.java
    override fun parse(s: String): BigDecimal = if ( s.isEmpty() ) BigDecimal.ZERO else BigDecimal(s)
    override fun toPreferences(value: BigDecimal): String = toString()
}


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
        prefBd = BigDecimal.ONE
        Log.e("ASD2", prefBd.toString())
        prefBd2 = BigDecimal.ONE
        Log.e("ASD2", prefBd2.toString())
    }

}

enum class MyEnum {
    Enum1, Enum2
}