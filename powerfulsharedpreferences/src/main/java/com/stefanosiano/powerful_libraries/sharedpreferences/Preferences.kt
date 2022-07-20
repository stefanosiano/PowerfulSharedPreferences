package com.stefanosiano.powerful_libraries.sharedpreferences

internal class BPreference(key: String, defaultValue: Boolean, prefName: String?) :
    PowerfulPreference<Boolean>(key, defaultValue, prefName) {
    override fun getPrefClass() = Boolean::class.java
    override fun parse(s: String) = s.toBooleanStrictOrNull() ?: defaultValue
    override fun toPreferences(value: Boolean): String = value.toString()
}

internal class BPreferenceNullable(key: String, defaultValue: Boolean?, prefName: String?) :
    PowerfulPreference<Boolean?>(key, defaultValue, prefName) {
    override fun getPrefClass() = Boolean::class.java
    override fun parse(s: String) = if (s.isNotEmpty()) s.toBooleanStrictOrNull() ?: defaultValue else defaultValue
    override fun toPreferences(value: Boolean?): String = value?.toString() ?: ""
}

internal class IPreference(key: String, defaultValue: Int, prefName: String?) :
    PowerfulPreference<Int>(key, defaultValue, prefName) {
    override fun getPrefClass() = Int::class.java
    override fun parse(s: String) = s.toIntOrNull() ?: defaultValue
    override fun toPreferences(value: Int): String = value.toString()
}

internal class IPreferenceNullable(key: String, defaultValue: Int?, prefName: String?) :
    PowerfulPreference<Int?>(key, defaultValue, prefName) {
    override fun getPrefClass() = Int::class.java
    override fun parse(s: String) = if (s.isNotEmpty()) s.toIntOrNull() ?: defaultValue else defaultValue
    override fun toPreferences(value: Int?): String = value?.toString() ?: ""
}

internal class LPreference(key: String, defaultValue: Long, prefName: String?) :
    PowerfulPreference<Long>(key, defaultValue, prefName) {
    override fun getPrefClass() = Long::class.java
    override fun parse(s: String) = s.toLongOrNull() ?: defaultValue
    override fun toPreferences(value: Long): String = value.toString()
}

internal class LPreferenceNullable(key: String, defaultValue: Long?, prefName: String?) :
    PowerfulPreference<Long?>(key, defaultValue, prefName) {
    override fun getPrefClass() = Long::class.java
    override fun parse(s: String) = if (s.isNotEmpty()) s.toLongOrNull() ?: defaultValue else defaultValue
    override fun toPreferences(value: Long?): String = value?.toString() ?: ""
}

internal class FPreference(key: String, defaultValue: Float, prefName: String?) :
    PowerfulPreference<Float>(key, defaultValue, prefName) {
    override fun getPrefClass() = Float::class.java
    override fun parse(s: String) = s.toFloatOrNull() ?: defaultValue
    override fun toPreferences(value: Float): String = value.toString()
}

internal class FPreferenceNullable(key: String, defaultValue: Float?, prefName: String?) :
    PowerfulPreference<Float?>(key, defaultValue, prefName) {
    override fun getPrefClass() = Float::class.java
    override fun parse(s: String) = if (s.isNotEmpty()) s.toFloatOrNull() ?: defaultValue else defaultValue
    override fun toPreferences(value: Float?): String = value?.toString() ?: ""
}

internal class DPreference(key: String, defaultValue: Double, prefName: String?) :
    PowerfulPreference<Double>(key, defaultValue, prefName) {
    override fun getPrefClass() = Double::class.java
    override fun parse(s: String) = s.toDoubleOrNull() ?: defaultValue
    override fun toPreferences(value: Double): String = value.toString()
}

internal class DPreferenceNullable(key: String, defaultValue: Double?, prefName: String?) :
    PowerfulPreference<Double?>(key, defaultValue, prefName) {
    override fun getPrefClass() = Double::class.java
    override fun parse(s: String) = if (s.isNotEmpty()) s.toDoubleOrNull() ?: defaultValue else defaultValue
    override fun toPreferences(value: Double?): String = value?.toString() ?: ""
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
    key: String,
    defaultValue: T,
    prefName: String?
) : PowerfulPreference<T>(key, defaultValue, prefName) {
    private val clazz: Class<T> = defaultValue.javaClass
    override fun getPrefClass() = clazz
    override fun parse(s: String): T = clazz.enumConstants?.firstOrNull { it.name == s } ?: defaultValue
    override fun toPreferences(value: T): String = value.name
}

internal class ObjPreference<T : Any>(
    key: String,
    defaultValue: T,
    prefName: String?,
    private val parsePref: (s: String) -> T,
    private val toPref: (t: T) -> String = { it.toString() }
) : PowerfulPreference<T>(key, defaultValue, prefName) {
    override fun getPrefClass() = defaultValue::class.java
    override fun parse(s: String): T = parsePref(s)
    override fun toPreferences(value: T): String = toPref(value)
}

internal class ObjPreferenceNullable<T>(
    private val clazz: Class<T>,
    key: String,
    defaultValue: T?,
    prefName: String?,
    private val parsePref: (s: String) -> T?,
    private val toPref: (t: T?) -> String = { it?.toString() ?: "" }
) : PowerfulPreference<T?>(key, defaultValue, prefName) {
    override fun getPrefClass(): Class<*> = clazz
    override fun parse(s: String): T? = parsePref(s)
    override fun toPreferences(value: T?): String = toPref(value)
}
