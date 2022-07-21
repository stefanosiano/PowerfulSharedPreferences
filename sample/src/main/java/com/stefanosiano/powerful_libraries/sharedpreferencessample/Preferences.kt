package com.stefanosiano.powerful_libraries.sharedpreferencessample

import com.stefanosiano.powerful_libraries.sharedpreferences.PowerfulPreference
import com.stefanosiano.powerful_libraries.sharedpreferences.Prefs
import java.math.BigDecimal

internal object Preferences {
    var preference1 by Prefs.newPref("p1", 1)
    val preference2 = Prefs.newPref("p2", 1.0)
    var preference3 by Prefs.newEnumPref("p3", MyEnum.Enum1)
    val preference5: PowerfulPreference<MyClass> = MyClassPreference("p5", MyClass(""))
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

    class MyClassPreference : PowerfulPreference<MyClass> {
        constructor(key: String, defaultValue: MyClass, prefName: String) : super(key, defaultValue, prefName)
        constructor(key: String, defaultValue: MyClass) : super(key, defaultValue)
        override fun getPrefClass(): Class<*> = MyClass::class.java
        override fun parse(s: String): MyClass = MyClass(s)
        override fun toPreferences(value: MyClass): String = value.text
    }
    var prefMc3 by MyClassPreference("prefMc3", MyClass(""))

    init {
        preference5.put(MyClass("aaaaaa"))
    }
}

internal enum class MyEnum {
    Enum1, Enum2
}
