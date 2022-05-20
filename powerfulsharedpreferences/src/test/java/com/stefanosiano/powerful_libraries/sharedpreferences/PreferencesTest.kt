package com.stefanosiano.powerful_libraries.sharedpreferences

import org.junit.Test
import kotlin.test.assertTrue

internal class BPreferenceTest : BaseTest() {

    private val pref = BPreference("key", false, "prefName")
    private val pref2 = BPreference("key2", true, "prefName2")

    @Test
    fun getPrefClass() {
        assertTrue { pref.getPrefClass() == Boolean::class.java }
    }

    @Test
    fun parse() {
        assertTrue { !pref.parse("false") }
        assertTrue { pref.parse("true") }
        assertTrue { !pref2.parse("false") }
        assertTrue { pref2.parse("true") }
    }

    @Test
    fun `parse empty string returns default value`() {
        assertTrue { !pref.parse("") }
        assertTrue { pref2.parse("") }
    }

    @Test
    fun `parse invalid value returns default value`() {
        assertTrue { !pref.parse("fal") }
        assertTrue { pref2.parse("fal") }
    }

    @Test
    fun toPreferences() {
        assertTrue { pref.toPreferences(true) == "true" }
        assertTrue { pref.toPreferences(false) == "false" }
        assertTrue { pref2.toPreferences(true) == "true" }
        assertTrue { pref2.toPreferences(false) == "false" }
    }
}

class IPreferenceTest : BaseTest() {

    private val pref = IPreference("key", 10, "prefName")

    @Test
    fun getPrefClass() {
        assertTrue { pref.getPrefClass() == Int::class.java }
    }

    @Test
    fun parse() {
        assertTrue { pref.parse("2") == 2 }
    }

    @Test
    fun `parse empty string returns default value`() {
        assertTrue { pref.parse("") == 10 }
    }

    @Test
    fun `parse invalid value returns default value`() {
        assertTrue { pref.parse("a") == 10 }
    }

    @Test
    fun toPreferences() {
        assertTrue { pref.toPreferences(1) == "1" }
        assertTrue { pref.toPreferences(4) == "4" }
    }
}

class LPreferenceTest : BaseTest() {

    private val pref = LPreference("key", 10L, "prefName")

    @Test
    fun getPrefClass() {
        assertTrue { pref.getPrefClass() == Long::class.java }
    }

    @Test
    fun parse() {
        assertTrue { pref.parse("2") == 2L }
    }

    @Test
    fun `parse empty string returns default value`() {
        assertTrue { pref.parse("") == 10L }
    }

    @Test
    fun `parse invalid value returns default value`() {
        assertTrue { pref.parse("a") == 10L }
    }

    @Test
    fun toPreferences() {
        assertTrue { pref.toPreferences(1L) == "1" }
        assertTrue { pref.toPreferences(4L) == "4" }
    }
}

class FPreferenceTest : BaseTest() {

    private val pref = FPreference("key", 10F, "prefName")

    @Test
    fun getPrefClass() {
        assertTrue { pref.getPrefClass() == Float::class.java }
    }

    @Test
    fun parse() {
        assertTrue { pref.parse("2") == 2F }
    }

    @Test
    fun `parse empty string returns default value`() {
        assertTrue { pref.parse("") == 10F }
    }

    @Test
    fun `parse invalid value returns default value`() {
        assertTrue { pref.parse("a") == 10F }
    }

    @Test
    fun toPreferences() {
        assertTrue { pref.toPreferences(1F) == "1" }
        assertTrue { pref.toPreferences(4F) == "4" }
    }
}

class DPreferenceTest : BaseTest() {

    private val pref = DPreference("key", 10.0, "prefName")

    @Test
    fun getPrefClass() {
        assertTrue { pref.getPrefClass() == Double::class.java }
    }

    @Test
    fun parse() {
        assertTrue { pref.parse("2") == 2.0 }
    }

    @Test
    fun `parse empty string returns default value`() {
        assertTrue { pref.parse("") == 10.0 }
    }

    @Test
    fun `parse invalid value returns default value`() {
        assertTrue { pref.parse("a") == 10.0 }
    }

    @Test
    fun toPreferences() {
        assertTrue { pref.toPreferences(1.0) == "1" }
        assertTrue { pref.toPreferences(4.0) == "4" }
    }
}

class SPreferenceTest : BaseTest() {

    private val pref = SPreference("key", "10", "prefName")

    @Test
    fun getPrefClass() {
        assertTrue { pref.getPrefClass() == String::class.java }
    }

    @Test
    fun parse() {
        assertTrue { pref.parse("") == "" }
        assertTrue { pref.parse("2") == "2" }
        assertTrue { pref.parse("a") == "a" }
    }

    @Test
    fun toPreferences() {
        assertTrue { pref.toPreferences("1") == "1" }
        assertTrue { pref.toPreferences("4") == "4" }
    }
}

class DummyPreferenceTest : BaseTest() {

    private val pref = DummyPreference("key", 10, "prefName")

    @Test
    fun getPrefClass() {
        assertTrue { pref.getPrefClass() == String::class.java }
    }

    @Test
    fun parse() {
        assertTrue { pref.parse("") == "" }
        assertTrue { pref.parse("2") == "2" }
        assertTrue { pref.parse("a") == "a" }
    }

    @Test
    fun toPreferences() {
        assertTrue { pref.toPreferences("1") == "1" }
        assertTrue { pref.toPreferences("4") == "4" }
    }
}

class EnumPreferenceTest : BaseTest() {

    private enum class EnumPref { a, b, c }
    private val pref = EnumPreference("key", EnumPref.a, "prefName")

    @Test
    fun getPrefClass() {
        assertTrue { pref.getPrefClass() == EnumPref::class.java }
    }

    @Test
    fun parse() {
        assertTrue { pref.parse(EnumPref.c.name) == EnumPref.c }
    }

    @Test
    fun `parse empty string returns default value`() {
        assertTrue { pref.parse("") == EnumPref.a }
    }

    @Test
    fun `parse invalid value returns default value`() {
        assertTrue { pref.parse("2") == EnumPref.a }
    }

    @Test
    fun toPreferences() {
        assertTrue { pref.toPreferences(EnumPref.b) == EnumPref.b.name }
        assertTrue { pref.toPreferences(EnumPref.c) == EnumPref.c.name }
    }
}

class ObjPreferenceTest : BaseTest() {

    private data class MyClass(var first: String, var second: String) {
        constructor(s: String) :
                this(s.split(":").getOrNull(0) ?: "", s.split(":").getOrNull(1) ?: "")
        fun toPref() = "$first:$second"
    }

    private val myObj = MyClass("first:second")

    private val pref = ObjPreference("key", myObj, "prefName",
        parsePref = { MyClass(it) },
        toPref = { it.toPref() }
    )

    @Test
    fun getPrefClass() {
        assertTrue { pref.getPrefClass() == MyClass::class.java }
    }

    @Test
    fun parse() {
        val expected = myObj
        expected.first = ""
        expected.second = ""
        assertTrue { pref.parse("") == expected }
        expected.first = "a"
        expected.second = ""
        assertTrue { pref.parse("a") == expected }
        expected.first = "2"
        expected.second = ""
        assertTrue { pref.parse("2") == expected }
        expected.first = "a"
        expected.second = "ss"
        assertTrue { pref.parse("a:ss") == expected }
    }

    @Test
    fun toPreferences() {
        assertTrue { pref.toPreferences(myObj) == "${myObj.first}:${myObj.second}" }
        assertTrue { pref.toPreferences(myObj) == myObj.toPref() }
    }
}
