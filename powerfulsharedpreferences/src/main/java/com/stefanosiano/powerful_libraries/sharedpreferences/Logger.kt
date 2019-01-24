package com.stefanosiano.powerful_libraries.sharedpreferences

import android.util.Log
import java.util.HashMap
import com.stefanosiano.powerful_libraries.sharedpreferences.Prefs.Builder.Companion.LOG_ERRORS
import com.stefanosiano.powerful_libraries.sharedpreferences.Prefs.Builder.Companion.LOG_VALUES
import com.stefanosiano.powerful_libraries.sharedpreferences.Prefs.Builder.Companion.LOG_VERBOSE


internal object Logger {

    private var mLogLevel = 0
    private const val mTag = "Prefs"

    fun setLevel(logLevel: Int) { mLogLevel = logLevel }

    fun logBuild(defaultPrefsName: String, crypter: Crypter?, prefsMap: HashMap<String, PrefContainer>) {
        if (mLogLevel < Prefs.Builder.LOG_VERBOSE)
            return
        Log.v(mTag, "Initialized with default SharedPreferences $defaultPrefsName with encryption " + (crypter != null))
        for (prefContainer in prefsMap.values)
            Log.v(mTag, "Additional SharedPreferences files: ${prefContainer.name} with encryption " + (crypter != null && prefContainer.useCrypter))
    }

    fun logErrorChangeCrypter(e: Exception) = log(LOG_ERRORS, 4, "Trying to change crypter, but got an error: ${e.localizedMessage}\nNo values were changed!")

    fun logChangeCrypter() = log(LOG_VERBOSE, 1, "Crypter was changed, and all values have been encrypted")

    fun logTerminate() = log(LOG_VERBOSE, 1, "Terminating and releasing all objects in memory")

    fun logNewPref(key: String, defaultValue: String, classs: Class<*>?) = log(LOG_VERBOSE, 1, "Created preference $key : $defaultValue (${classs?.simpleName ?: "unknown"})")

    fun logGetAll() = log(LOG_VERBOSE, 1, "Retrieving all the preferences")

    fun logClear() = log(LOG_VERBOSE, 1, "Clearing all the preferences")

    fun logRemove(key: String) = log(LOG_VERBOSE, 1, "Removing $key")

    fun logGet(key: String, value: String, classs: Class<*>?) = log(LOG_VALUES, 1, "Retrieved $key : $value (${classs?.simpleName ?: "unknown"})")

    fun logGetCached(key: String, value: String, classs: Class<*>?) = log(LOG_VALUES, 1, "Retrieved from cache $key : $value (${classs?.simpleName ?: "unknown"})")

    fun logPut(key: String, value: String, classs: Class<*>?) = log(LOG_VALUES, 1, "Put $key : $value (${classs?.simpleName ?: "unknown"})")

    fun logContains(key: String, found: Boolean) = log(LOG_VERBOSE, 1, "Check existance of $key: $found")

    fun logDecryptException(e: Exception, key: String) = log(LOG_VERBOSE, 4, "Error decrypting $key \n${e.localizedMessage}")

    fun logEncryptException(e: Exception, key: String, value: String) = log(LOG_VERBOSE, 4, "Error encrypting $key : $value \n${e.localizedMessage}")

    fun logDecrypt(key: String, encryptedKey: String, encryptedValue: String, value: String) = log(LOG_VERBOSE, 2, "Retrieved $key : $value from $encryptedKey : $encryptedValue")

    fun logEncrypt(key: String, encryptedKey: String, encryptedValue: String, value: String) = log(LOG_VERBOSE, 2, "Saving $key : $value as $encryptedKey : $encryptedValue")

    fun logParseNotFound(key: String, defaultValue: String) = log(LOG_ERRORS, 3, "No data found for key $key. Returning default value: $defaultValue")

    fun logParseNumberException(e: NumberFormatException, key: String, value: String, defaultValue: String, classs: Class<*>?) = log(LOG_ERRORS, 4, "Error trying to parse $key : $value as ${classs?.simpleName ?: "unknown"}. ${e.localizedMessage}\nReturning default value: $defaultValue")

    fun logParseTypeException(key: String, value: String, defaultValue: String, classs: Class<*>?) = log(LOG_ERRORS, 4, "Don't know hot to parse $key : '$value' as ${classs?.simpleName ?: "unknown"}. Returning default value: $defaultValue")


    private fun log(logLevel: Int, logType: Int, logText: String) {
        if (mLogLevel < logLevel) return
        when(logType) {
            1 -> Log.d(mTag, logText)
            2 -> Log.v(mTag, logText)
            3 -> Log.w(mTag, logText)
            4 -> Log.e(mTag, logText)
        }
    }
}
