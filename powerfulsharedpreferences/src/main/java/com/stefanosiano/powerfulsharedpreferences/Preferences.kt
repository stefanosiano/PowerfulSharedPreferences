package com.stefanosiano.powerfulsharedpreferences


internal class BPreference(key: String, defaultValue: Boolean, prefName: String?) : PowerfulPreference<Boolean>(key, defaultValue, prefName) {
    override fun getPrefClass() = Boolean::class.java
    override fun parse(s: String) = s.toBoolean()
}

internal class IPreference(key: String, defaultValue: Int, prefName: String?) : PowerfulPreference<Int>(key, defaultValue, prefName) {
    override fun getPrefClass() = Int::class.java
    override fun parse(s: String) = s.toInt()
}

internal class LPreference(key: String, defaultValue: Long, prefName: String?) : PowerfulPreference<Long>(key, defaultValue, prefName) {
    override fun getPrefClass() = Long::class.java
    override fun parse(s: String) = s.toLong()
}

internal class FPreference(key: String, defaultValue: Float, prefName: String?) : PowerfulPreference<Float>(key, defaultValue, prefName) {
    override fun getPrefClass() = Float::class.java
    override fun parse(s: String) = s.toFloat()
}

internal class DPreference(key: String, defaultValue: Double, prefName: String?) : PowerfulPreference<Double>(key, defaultValue, prefName) {
    override fun getPrefClass() = Double::class.java
    override fun parse(s: String) = s.toDouble()
}

internal class SPreference(key: String, defaultValue: String, prefName: String?) : PowerfulPreference<String>(key, defaultValue, prefName) {
    override fun getPrefClass() = String::class.java
    override fun parse(s: String) = s
}