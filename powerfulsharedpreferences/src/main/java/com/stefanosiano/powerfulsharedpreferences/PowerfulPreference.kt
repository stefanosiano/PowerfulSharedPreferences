package com.stefanosiano.powerfulsharedpreferences


abstract class PowerfulPreference<T>(

        /** Returns the key of the preference  */
        val key: String,
        /** Returns the default value of the preference  */
        val defaultValue: T,
        /** Returns the file name associated to this preference  */
        val preferencesFileName: String?
) {

    protected constructor(key: String, defaultValue: T) : this(key, defaultValue, null)

    /** Returns the key of the cache map of the preferences  */
    fun getCacheMapKey() = "$preferencesFileName$$key"

    /** Returns the class of the value to save/retrieve  */
    abstract fun getPrefClass(): Class<*>

    /** Returns the data of the preference from a string. Exceptions are handled by the library itself  */
    abstract fun parse(s: String): T

    /** Returns the value of this preference  */
    fun get(): T = Prefs.get(this)

    /** Puts a value to this preference  */
    fun put(value: T) = Prefs.put(this, value)
}
