package com.stefanosiano.powerful_libraries.sharedpreferences

import android.content.Context
import android.content.SharedPreferences

internal class PrefContainer(var useCrypter: Boolean, var name: String, private var mode: Int) {
    var sharedPreferences: SharedPreferences? = null
    fun build(context: Context) { sharedPreferences = context.applicationContext.getSharedPreferences(name, mode) }
}
