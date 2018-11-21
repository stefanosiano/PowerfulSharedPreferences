package com.stefanosiano.powerfulsharedpreferences


abstract class PowerfulPreference<T>(

        /** Returns the key of the preference  */
        val key: String,
        /** Returns the default value of the preference  */
        val defaultValue: T,
        /** Returns the file name associated to this preference  */
        val preferencesFileName: String?
) {

    /** List of callbacks to call when the preference changes */
    private val changeCallbacks = ArrayList<(value: T) -> Unit>()

    protected constructor(key: String, defaultValue: T) : this(key, defaultValue, null)

    /** Observes the preference. When it changes, the function is called. When preferences are cleared, the function is NOT called */
    fun observe(onChange: (value: T) -> Unit): PowerfulPreference<T> { changeCallbacks.add(onChange); return this }

    /** Stops observing the preference */
    fun stopObserve(onChange: (value: T) -> Unit) = changeCallbacks.remove(onChange)

    internal fun callOnChange(value: T) { changeCallbacks.forEach{ it.invoke(value) } }

    /** Returns the key of the cache map of the preferences  */
    internal fun getCacheMapKey() = "$preferencesFileName$$key"

    /** Returns the class of the value to save/retrieve. Used only for logs. If null, logs will show 'Unknown' as object class.  */
    open fun getPrefClass(): Class<*>? = null

    /** Returns the data of the preference from a string. Exceptions are handled by the library itself  */
    abstract fun parse(s: String): T

    /** Parses the value and then returns the string to put in the preferences */
    open fun toPreferences(value: T): String = value.toString()

    /** Returns the value of this preference  */
    fun get(): T = Prefs.get(this)

    /** Puts a value to this preference  */
    fun put(value: T) = Prefs.put(this, value)
}
