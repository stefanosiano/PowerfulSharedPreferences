package com.stefanosiano.powerful_libraries.sharedpreferences

import android.util.Log
import com.stefanosiano.powerful_libraries.sharedpreferences.Prefs.Builder.Companion.LOG_ERRORS
import com.stefanosiano.powerful_libraries.sharedpreferences.Prefs.Builder.Companion.LOG_VALUES
import com.stefanosiano.powerful_libraries.sharedpreferences.Prefs.Builder.Companion.LOG_VERBOSE

private const val TAG = "Prefs"

internal class Logger {

    private var mLogLevel = 0

    fun setLevel(logLevel: Int) { mLogLevel = logLevel }

    fun logD(m: String) {
        if (mLogLevel >= LOG_VALUES) {
            Log.d(TAG, m)
        }
    }

    fun logV(m: String) {
        if (mLogLevel >= LOG_VERBOSE) {
            Log.v(TAG, m)
        }
    }

    fun logE(m: String) {
        if (mLogLevel >= LOG_ERRORS) {
            Log.e(TAG, m)
        }
    }

    companion object {
        private var mLogger = Logger()

        fun getInstance(): Logger = mLogger
    }
}
