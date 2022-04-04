package com.stefanosiano.powerful_libraries.sharedpreferences

import android.util.Log
import com.stefanosiano.powerful_libraries.sharedpreferences.Prefs.Builder.Companion.LOG_ERRORS
import com.stefanosiano.powerful_libraries.sharedpreferences.Prefs.Builder.Companion.LOG_VALUES
import com.stefanosiano.powerful_libraries.sharedpreferences.Prefs.Builder.Companion.LOG_VERBOSE


internal object Logger {

    private var mLogLevel = 0
    private const val mTag = "Prefs"

    fun setLevel(logLevel: Int) { mLogLevel = logLevel }

    fun logD(m: String, level: Int= LOG_VALUES) {
        if (mLogLevel >= level) {
            Log.d(mTag, m)
        }
    }

    fun logV(m: String, level: Int = LOG_VERBOSE) {
        if (mLogLevel >= level) {
            Log.v(mTag, m)
        }
    }

    fun logE(m: String, level: Int = LOG_ERRORS) {
        if (mLogLevel >= level) {
            Log.e(mTag, m)
        }
    }
}
