package com.stefanosiano.powerful_libraries.sharedpreferences

internal class BPreference(key: String, defaultValue: Boolean, prefName: String?) :
    PowerfulPreference<Boolean>(key, defaultValue, prefName) {
    override fun getPrefClass() = Boolean::class.java
    override fun parse(s: String) = s.toBoolean()
    override fun toPreferences(value: Boolean): String = value.toString()
}

internal class IPreference(key: String, defaultValue: Int, prefName: String?) :
    PowerfulPreference<Int>(key, defaultValue, prefName) {
    override fun getPrefClass() = Int::class.java
    override fun parse(s: String) = s.toInt()
    override fun toPreferences(value: Int): String = value.toString()
}

internal class LPreference(key: String, defaultValue: Long, prefName: String?) :
    PowerfulPreference<Long>(key, defaultValue, prefName) {
    override fun getPrefClass() = Long::class.java
    override fun parse(s: String) = s.toLong()
    override fun toPreferences(value: Long): String = value.toString()
}

internal class FPreference(key: String, defaultValue: Float, prefName: String?) :
    PowerfulPreference<Float>(key, defaultValue, prefName) {
    override fun getPrefClass() = Float::class.java
    override fun parse(s: String) = s.toFloat()
    override fun toPreferences(value: Float): String = value.toString()
}

internal class DPreference(key: String, defaultValue: Double, prefName: String?) :
    PowerfulPreference<Double>(key, defaultValue, prefName) {
    override fun getPrefClass() = Double::class.java
    override fun parse(s: String) = s.toDouble()
    override fun toPreferences(value: Double): String = value.toString()
}

internal class SPreference(key: String, defaultValue: String, prefName: String?) :
    PowerfulPreference<String>(key, defaultValue, prefName) {
    override fun getPrefClass() = String::class.java
    override fun parse(s: String) = s
    override fun toPreferences(value: String): String = value
}

internal class DummyPreference(key: String, defaultValue: Any?, prefName: String?) :
    PowerfulPreference<String>(key, defaultValue?.toString() ?: "", prefName) {
    override fun getPrefClass() = String::class.java
    override fun parse(s: String) = s
    override fun toPreferences(value: String): String = value
}

internal class EnumPreference<T : Enum<T>>(
    private val clazz: Class<T>,
    key: String,
    defaultValue: T,
    prefName: String?
) : PowerfulPreference<T>(key, defaultValue, prefName) {
    override fun getPrefClass() = clazz
    override fun parse(s: String): T = clazz.enumConstants?.first { it.name == s }!!
    override fun toPreferences(value: T): String = value.name
}

internal class ObjPreference<T : PrefObj>(
    private val clazz: Class<T>,
    key: String,
    defaultValue: T,
    prefName: String?
) : PowerfulPreference<T>(key, defaultValue, prefName) {
    override fun getPrefClass() = clazz
    override fun parse(s: String): T = clazz.newInstance().fromPreferences(s) as T
    override fun toPreferences(value: T): String = value.toPreferences()
}

/**
 * Interface used to easily create any object to store in SharedPreferences.
 * It needs only two methods:
 * [fromPreferences] -> recreates the object from the string stored in the preferences.
 * [toPreferences] -> creates the string that represents this object to store in the preferences.
 */
interface PrefObj {
    /** Recreates the object from the string stored in the preferences. */
    fun fromPreferences(s: String): PrefObj

    /** Creates the string that represents this object to store in the preferences. */
    fun toPreferences(): String
}
