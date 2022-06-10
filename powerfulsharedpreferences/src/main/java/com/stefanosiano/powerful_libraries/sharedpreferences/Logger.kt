package com.stefanosiano.powerful_libraries.sharedpreferences

import android.util.Log
import com.stefanosiano.powerful_libraries.sharedpreferences.Prefs.Builder.Companion.LOG_ERRORS
import com.stefanosiano.powerful_libraries.sharedpreferences.Prefs.Builder.Companion.LOG_VALUES
import com.stefanosiano.powerful_libraries.sharedpreferences.Prefs.Builder.Companion.LOG_VERBOSE

private const val mTag = "Prefs"

internal class Logger {

    companion object {
        private var logger = Logger()

        fun getInstance(): Logger = logger
    }

    private var mLogLevel = 0

    fun setLevel(logLevel: Int) { mLogLevel = logLevel }

    fun logD(m: String) {
        if (mLogLevel >= LOG_VALUES) {
            Log.d(mTag, m)
        }
    }

    fun logV(m: String) {
        if (mLogLevel >= LOG_VERBOSE) {
            Log.v(mTag, m)
        }
    }

    fun logE(m: String) {
        if (mLogLevel >= LOG_ERRORS) {
            Log.e(mTag, m)
        }
    }
}
