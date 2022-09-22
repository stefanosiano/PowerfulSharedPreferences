package com.stefanosiano.powerful_libraries.sharedpreferences

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class BPreferenceTest : BaseTest() {

    private val pref = BPreference("key", false, "prefName")
    private val pref2 = BPreference("key2", true, "prefName2")

    @Test
    fun getPrefClass() {
        assertEquals(Boolean::class.java, pref.getPrefClass())
    }

    @Test
    fun parse() {
        assertFalse(pref.parse("false"))
        assertTrue(pref.parse("true"))
        assertFalse(pref2.parse("false"))
        assertTrue(pref2.parse("true"))
    }

    @Test
    fun `parse empty string returns default value`() {
        assertFalse(pref.parse(""))
        assertTrue(pref2.parse(""))
    }

    @Test
    fun `parse invalid value returns default value`() {
        assertFalse(pref.parse("fal"))
        assertTrue(pref2.parse("fal"))
    }

    @Test
    fun toPreferences() {
        assertEquals("true", pref.toPreferences(true))
        assertEquals("false", pref.toPreferences(false))
        assertEquals("true", pref2.toPreferences(true))
        assertEquals("false", pref2.toPreferences(false))
    }
}

internal class BPreferenceNullableTest : BaseTest() {

    private val pref = BPreferenceNullable("key", false, "prefName")
    private val pref2 = BPreferenceNullable("key2", null, "prefName2")

    @Test
    fun getPrefClass() {
        assertEquals(Boolean::class.java, pref.getPrefClass())
        assertEquals(Boolean::class.java, pref2.getPrefClass())
    }

    @Test
    fun parse() {
        assertEquals(false, pref.parse("false"))
        assertEquals(true, pref.parse("true"))
        assertEquals(false, pref2.parse("false"))
        assertEquals(true, pref2.parse("true"))
    }

    @Test
    fun `parse empty string returns default value`() {
        assertEquals(false, pref.parse(""))
        assertNull(pref2.parse(""))
    }

    @Test
    fun `parse invalid value returns default value`() {
        assertEquals(false, pref.parse("fal"))
        assertNull(pref2.parse("fal"))
    }

    @Test
    fun toPreferences() {
        assertEquals("true", pref.toPreferences(true))
        assertEquals("false", pref.toPreferences(false))
        assertEquals("true", pref2.toPreferences(true))
        assertEquals("false", pref2.toPreferences(false))
        assertEquals("", pref.toPreferences(null))
        assertEquals("", pref2.toPreferences(null))
    }
}

class IPreferenceTest : BaseTest() {

    private val pref = IPreference("key", 10, "prefName")

    @Test
    fun getPrefClass() {
        assertEquals(Int::class.java, pref.getPrefClass())
    }

    @Test
    fun parse() {
        assertEquals(2, pref.parse("2"))
    }

    @Test
    fun `parse empty string returns default value`() {
        assertEquals(10, pref.parse(""))
    }

    @Test
    fun `parse invalid value returns default value`() {
        assertEquals(10, pref.parse("a"))
    }

    @Test
    fun toPreferences() {
        assertEquals("1", pref.toPreferences(1))
        assertEquals("4", pref.toPreferences(4))
    }
}

class IPreferenceNullableTest : BaseTest() {

    private val pref = IPreferenceNullable("key", 10, "prefName")
    private val pref2 = IPreferenceNullable("key", null, "prefName2")

    @Test
    fun getPrefClass() {
        assertEquals(Int::class.java, pref.getPrefClass())
        assertEquals(Int::class.java, pref2.getPrefClass())
    }

    @Test
    fun parse() {
        assertEquals(2, pref.parse("2"))
        assertEquals(2, pref2.parse("2"))
    }

    @Test
    fun `parse empty string returns default value`() {
        assertEquals(10, pref.parse(""))
        assertNull(pref2.parse(""))
    }

    @Test
    fun `parse invalid value returns default value`() {
        assertEquals(10, pref.parse("a"))
        assertNull(pref2.parse("a"))
    }

    @Test
    fun toPreferences() {
        assertEquals("1", pref.toPreferences(1))
        assertEquals("4", pref2.toPreferences(4))
        assertEquals("", pref.toPreferences(null))
        assertEquals("", pref2.toPreferences(null))
    }
}

class LPreferenceTest : BaseTest() {

    private val pref = LPreference("key", 10L, "prefName")

    @Test
    fun getPrefClass() {
        assertEquals(Long::class.java, pref.getPrefClass())
    }

    @Test
    fun parse() {
        assertEquals(2L, pref.parse("2"))
    }

    @Test
    fun `parse empty string returns default value`() {
        assertEquals(10L, pref.parse(""))
    }

    @Test
    fun `parse invalid value returns default value`() {
        assertEquals(10L, pref.parse("a"))
    }

    @Test
    fun toPreferences() {
        assertEquals("1", pref.toPreferences(1L))
        assertEquals("4", pref.toPreferences(4L))
    }
}

class LPreferenceNullableTest : BaseTest() {

    private val pref = LPreferenceNullable("key", 10L, "prefName")
    private val pref2 = LPreferenceNullable("key", null, "prefName2")

    @Test
    fun getPrefClass() {
        assertEquals(Long::class.java, pref.getPrefClass())
        assertEquals(Long::class.java, pref2.getPrefClass())
    }

    @Test
    fun parse() {
        assertEquals(2L, pref.parse("2"))
        assertEquals(2L, pref2.parse("2"))
    }

    @Test
    fun `parse empty string returns default value`() {
        assertEquals(10L, pref.parse(""))
        assertNull(pref2.parse(""))
    }

    @Test
    fun `parse invalid value returns default value`() {
        assertEquals(10L, pref.parse("a"))
        assertNull(pref2.parse("a"))
    }

    @Test
    fun toPreferences() {
        assertEquals("1", pref.toPreferences(1L))
        assertEquals("4", pref.toPreferences(4L))
        assertEquals("1", pref2.toPreferences(1L))
        assertEquals("4", pref2.toPreferences(4L))
        assertEquals("", pref.toPreferences(null))
        assertEquals("", pref2.toPreferences(null))
    }
}

class FPreferenceTest : BaseTest() {

    private val pref = FPreference("key", 10F, "prefName")

    @Test
    fun getPrefClass() {
        assertEquals(Float::class.java, pref.getPrefClass())
    }

    @Test
    fun parse() {
        assertEquals(2F, pref.parse("2"))
    }

    @Test
    fun `parse empty string returns default value`() {
        assertEquals(10F, pref.parse(""))
    }

    @Test
    fun `parse invalid value returns default value`() {
        assertEquals(10F, pref.parse("a"))
    }

    @Test
    fun toPreferences() {
        assertEquals("1.0", pref.toPreferences(1F))
        assertEquals("4.0", pref.toPreferences(4F))
    }
}

class FPreferenceNullableTest : BaseTest() {

    private val pref = FPreferenceNullable("key", 10F, "prefName")
    private val pref2 = FPreferenceNullable("key", null, "prefName2")

    @Test
    fun getPrefClass() {
        assertEquals(Float::class.java, pref.getPrefClass())
        assertEquals(Float::class.java, pref2.getPrefClass())
    }

    @Test
    fun parse() {
        assertEquals(2F, pref.parse("2"))
        assertEquals(2F, pref2.parse("2"))
    }

    @Test
    fun `parse empty string returns default value`() {
        assertEquals(10F, pref.parse(""))
        assertNull(pref2.parse(""))
    }

    @Test
    fun `parse invalid value returns default value`() {
        assertEquals(10F, pref.parse("a"))
        assertNull(pref2.parse("a"))
    }

    @Test
    fun toPreferences() {
        assertEquals("1.0", pref.toPreferences(1F))
        assertEquals("4.0", pref.toPreferences(4F))
        assertEquals("1.0", pref2.toPreferences(1F))
        assertEquals("4.0", pref2.toPreferences(4F))
        assertEquals("", pref.toPreferences(null))
        assertEquals("", pref2.toPreferences(null))
    }
}

class DPreferenceTest : BaseTest() {

    private val pref = DPreference("key", 10.0, "prefName")

    @Test
    fun getPrefClass() {
        assertEquals(Double::class.java, pref.getPrefClass())
    }

    @Test
    fun parse() {
        assertEquals(2.0, pref.parse("2"))
    }

    @Test
    fun `parse empty string returns default value`() {
        assertEquals(10.0, pref.parse(""))
    }

    @Test
    fun `parse invalid value returns default value`() {
        assertEquals(10.0, pref.parse("a"))
    }

    @Test
    fun toPreferences() {
        assertEquals("1.0", pref.toPreferences(1.0))
        assertEquals("4.0", pref.toPreferences(4.0))
    }
}

class DPreferenceNullableTest : BaseTest() {

    private val pref = DPreferenceNullable("key", 10.0, "prefName")
    private val pref2 = DPreferenceNullable("key", null, "prefName2")

    @Test
    fun getPrefClass() {
        assertEquals(Double::class.java, pref.getPrefClass())
        assertEquals(Double::class.java, pref2.getPrefClass())
    }

    @Test
    fun parse() {
        assertEquals(2.0, pref.parse("2"))
        assertEquals(2.0, pref2.parse("2"))
    }

    @Test
    fun `parse empty string returns default value`() {
        assertEquals(10.0, pref.parse(""))
        assertNull(pref2.parse(""))
    }

    @Test
    fun `parse invalid value returns default value`() {
        assertEquals(10.0, pref.parse("a"))
        assertNull(pref2.parse("a"))
    }

    @Test
    fun toPreferences() {
        assertEquals("1.0", pref.toPreferences(1.0))
        assertEquals("4.0", pref.toPreferences(4.0))
        assertEquals("1.0", pref2.toPreferences(1.0))
        assertEquals("4.0", pref2.toPreferences(4.0))
        assertEquals("", pref.toPreferences(null))
        assertEquals("", pref2.toPreferences(null))
    }
}

class SPreferenceTest : BaseTest() {

    private val pref = SPreference("key", "10", "prefName")

    @Test
    fun getPrefClass() {
        assertEquals(String::class.java, pref.getPrefClass())
    }

    @Test
    fun parse() {
        assertEquals("", pref.parse(""))
        assertEquals("2", pref.parse("2"))
        assertEquals("a", pref.parse("a"))
    }

    @Test
    fun toPreferences() {
        assertEquals("1", pref.toPreferences("1"))
        assertEquals("4", pref.toPreferences("4"))
    }
}

class DummyPreferenceTest : BaseTest() {

    private val pref = DummyPreference("key", 10, "prefName")

    @Test
    fun getPrefClass() {
        assertEquals(String::class.java, pref.getPrefClass())
    }

    @Test
    fun parse() {
        assertEquals("", pref.parse(""))
        assertEquals("2", pref.parse("2"))
        assertEquals("a", pref.parse("a"))
    }

    @Test
    fun toPreferences() {
        assertEquals("1", pref.toPreferences("1"))
        assertEquals("4", pref.toPreferences("4"))
    }
}

class EnumPreferenceTest : BaseTest() {

    private enum class EnumPref { A, B, C }
    private val pref = EnumPreference("key", EnumPref.A, "prefName")

    @Test
    fun getPrefClass() {
        assertEquals(EnumPref::class.java, pref.getPrefClass())
    }

    @Test
    fun parse() {
        assertEquals(EnumPref.C, pref.parse(EnumPref.C.name))
    }

    @Test
    fun `parse empty string returns default value`() {
        assertEquals(EnumPref.A, pref.parse(""))
    }

    @Test
    fun `parse invalid value returns default value`() {
        assertEquals(EnumPref.A, pref.parse("2"))
    }

    @Test
    fun toPreferences() {
        assertEquals(EnumPref.B.name, pref.toPreferences(EnumPref.B))
        assertEquals(EnumPref.C.name, pref.toPreferences(EnumPref.C))
    }
}

class ObjPreferenceTest : BaseTest() {

    private class MyClass(var first: String, var second: String) {
        constructor(s: String) :
            this(s.split(":").getOrNull(0) ?: "", s.split(":").getOrNull(1) ?: "")
        fun toPref() = "$first:$second"

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as MyClass

            if (first != other.first) return false
            if (second != other.second) return false

            return true
        }

        override fun hashCode(): Int {
            var result = first.hashCode()
            result = 31 * result + second.hashCode()
            return result
        }
    }

    private val myObj = MyClass("first:second")

    private val pref = ObjPreference(
        "key",
        myObj,
        "prefName",
        parsePref = { MyClass(it) },
        toPref = { it.toPref() }
    )

    @Test
    fun getPrefClass() {
        assertEquals(MyClass::class.java, pref.getPrefClass())
    }

    @Test
    fun parse() {
        val expected = myObj
        expected.first = ""
        expected.second = ""
        assertEquals(expected, pref.parse(""))
        expected.first = "a"
        expected.second = ""
        assertEquals(expected, pref.parse("a"))
        expected.first = "2"
        expected.second = ""
        assertEquals(expected, pref.parse("2"))
        expected.first = "a"
        expected.second = "ss"
        assertEquals(expected, pref.parse("a:ss"))
    }

    @Test
    fun toPreferences() {
        assertEquals("${myObj.first}:${myObj.second}", pref.toPreferences(myObj))
        assertEquals(myObj.toPref(), pref.toPreferences(myObj))
    }
}

class ObjPreferenceNullableTest : BaseTest() {

    private class MyClass(var first: String, var second: String) {
        constructor(s: String) :
            this(s.split(":").getOrNull(0) ?: "", s.split(":").getOrNull(1) ?: "")
        fun toPref() = "$first:$second"

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as MyClass

            if (first != other.first) return false
            if (second != other.second) return false

            return true
        }

        override fun hashCode(): Int {
            var result = first.hashCode()
            result = 31 * result + second.hashCode()
            return result
        }
    }

    private val myObj = MyClass("first:second")

    private val pref = ObjPreferenceNullable(
        MyClass::class.java,
        "key",
        myObj,
        "prefName",
        parsePref = { MyClass(it) },
        toPref = { it?.toPref() ?: "" }
    )

    private val pref2 = ObjPreferenceNullable(
        MyClass::class.java,
        "key",
        null,
        "prefName2",
        parsePref = { MyClass(it) },
        toPref = { it?.toPref() ?: "" }
    )

    @Test
    fun getPrefClass() {
        assertEquals(MyClass::class.java, pref.getPrefClass())
        assertEquals(MyClass::class.java, pref2.getPrefClass())
    }

    @Test
    fun parse() {
        val expected = myObj
        expected.first = ""
        expected.second = ""
        assertEquals(expected, pref.parse(""))
        assertEquals(expected, pref2.parse(""))
        expected.first = "a"
        expected.second = ""
        assertEquals(expected, pref.parse("a"))
        assertEquals(expected, pref2.parse("a"))
        expected.first = "2"
        expected.second = ""
        assertEquals(expected, pref.parse("2"))
        assertEquals(expected, pref2.parse("2"))
        expected.first = "a"
        expected.second = "ss"
        assertEquals(expected, pref.parse("a:ss"))
        assertEquals(expected, pref2.parse("a:ss"))
    }

    @Test
    fun toPreferences() {
        assertEquals("${myObj.first}:${myObj.second}", pref.toPreferences(myObj))
        assertEquals(myObj.toPref(), pref.toPreferences(myObj))
        assertEquals("${myObj.first}:${myObj.second}", pref2.toPreferences(myObj))
        assertEquals(myObj.toPref(), pref2.toPreferences(myObj))
        assertEquals("", pref.toPreferences(null))
        assertEquals("", pref2.toPreferences(null))
    }
}
