package com.stefanosiano.powerful_libraries.sharedpreferences

import android.content.Context
import android.content.SharedPreferences

internal class PrefContainer(var useObfuscator: Boolean, var name: String, private var mode: Int) {
    private var built = false
    var sharedPreferences: SharedPreferences? = null
    fun build(context: Context) {
        if (built) {
            return
        }
        sharedPreferences = context.applicationContext.getSharedPreferences(name, mode)
        built = true
    }
}
