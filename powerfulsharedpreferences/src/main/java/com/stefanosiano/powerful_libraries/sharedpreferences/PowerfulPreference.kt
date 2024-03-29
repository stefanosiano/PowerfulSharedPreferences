package com.stefanosiano.powerful_libraries.sharedpreferences

import kotlin.reflect.KProperty

/** Object that represents an object saved in the shared preferences. */
@Suppress("TooManyFunctions")
abstract class PowerfulPreference<T> (
    /** Returns the key of the preference. */
    val key: String,
    /** Returns the default value of the preference. */
    val defaultValue: T,
    /** Returns the file name associated to this preference. */
    val preferencesFileName: String?
) {

    init {
        @Suppress("unchecked_cast")
        Prefs.registerPreference(this as PowerfulPreference<Any?>)
    }

    /** List of callbacks to call when the preference changes. */
    private val changeCallbacks = ArrayList<(value: T) -> Unit>()

    protected constructor(key: String, defaultValue: T) : this(key, defaultValue, null)

    /**
     * Observes the preference. When it changes, the function is called.
     * NOTE: When preferences are cleared, the function is NOT called.
     */
    fun observe(onChange: (value: T) -> Unit): PowerfulPreference<T> { changeCallbacks.add(onChange); return this }

    /** Stops observing the preference. */
    fun stopObserve(onChange: (value: T) -> Unit) = changeCallbacks.remove(onChange)

    internal fun callOnChange(value: T) { changeCallbacks.forEach { it.invoke(value) } }

    internal fun callOnChange() { changeCallbacks.forEach { it.invoke(get()) } }

    /** Returns the key of the cache map of the preferences. */
    internal fun getCacheMapKey() = "$preferencesFileName$$key"

    /**
     * Returns the class of the value to save/retrieve. Used only for logs.
     * If null, logs will show 'Unknown' as object class.
     */
    open fun getPrefClass(): Class<*>? = defaultValue?.let { it::class.java }

    /** Returns the data of the preference from a string. Exceptions are handled by the library itself. */
    abstract fun parse(s: String): T

    /** Parses the value and then returns the string to put in the preferences. */
    open fun toPreferences(value: T): String = value.toString()

    /** Returns the value of this preference. */
    fun get(): T = Prefs[this]

    /** Puts a value to this preference. */
    fun put(value: T) = Prefs.put(this, value)

    /** Delegation to get the value from the preferences. */
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T = get()

    /** Delegation to put the value in the preferences. */
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) = put(value)

    internal fun getClassName() = getPrefClass()?.simpleName ?: "unknown"
}
