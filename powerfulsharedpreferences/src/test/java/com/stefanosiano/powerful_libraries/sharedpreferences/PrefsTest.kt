package com.stefanosiano.powerful_libraries.sharedpreferences

import android.content.Context
import java.math.BigDecimal
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

internal class PrefsTest : BaseTest() {

    private class Fixture {
        val prefFileName = "pref name 1"
        val altPrefFileName = "alternative preference file name"
        val plainPrefFileName = "plain preference file name"
        val prefIntDef = IPreference("pref2", 4, null)
        val prefStringAlt = SPreference("pref1", "default value", altPrefFileName)
        val prefStringPlain = SPreference("pref1", "old value", plainPrefFileName)
        val oldObfuscator = DefaultObfuscator("pass", "salt".toByteArray())
        val newObfuscator = DefaultObfuscator("newpass", "newsalt".toByteArray())
    }
    private enum class MyEnum { a, b, c }

    private lateinit var fixture: Fixture

    @BeforeTest
    fun initPrefs() {
        fixture = Fixture()
        Prefs
            .init(context)
            .setObfuscator(fixture.oldObfuscator)
            .addPrefs(fixture.prefFileName, Context.MODE_PRIVATE, true)
            .addPrefs(fixture.plainPrefFileName, Context.MODE_PRIVATE, false)
            .build()
    }

    @Test
    fun changeObfuscatorWithNullRemoveObfuscation() {
        Prefs.put("key", "value")
        Prefs.put(fixture.prefStringAlt, "value2")
        Prefs.put(fixture.prefIntDef, 10)
        val preferences = Prefs.getAllObfuscated()
        val oldRawKey = preferences.keys.first()
        val oldRawValue = preferences.values.first()
        val oldRawKey2 = preferences.keys.last()
        val oldRawValue2 = preferences.values.last()
        Prefs.changeObfuscator(null)
        assertEquals("value", Prefs.get("key"))
        assertEquals("value2", Prefs.get(fixture.prefStringAlt))
        assertEquals(10, Prefs.get(fixture.prefIntDef))
        val newPreferences = Prefs.getAllObfuscated()
        val newRawKey = newPreferences.keys.first()
        val newRawValue = newPreferences.values.first()
        val newRawKey2 = newPreferences.keys.last()
        val newRawValue2 = newPreferences.values.last()
        assertNotEquals(oldRawKey, newRawKey)
        assertEquals("pref2", newRawKey)
        assertNotEquals(oldRawValue, newRawValue)
        assertEquals("10", newRawValue)
        assertNotEquals(oldRawKey2, newRawKey2)
        assertEquals("key", newRawKey2)
        assertNotEquals(oldRawValue2, newRawValue2)
        assertEquals("value", newRawValue2)
    }

    @Test
    fun changeObfuscator() {
        Prefs.put("key", "value")
        Prefs.put(fixture.prefStringAlt, "value2")
        Prefs.put(fixture.prefIntDef, 10)
        val preferences = Prefs.getAllObfuscated()
        val oldRawKey = preferences.keys.first()
        val oldRawValue = preferences.values.first()
        val oldRawKey2 = preferences.keys.last()
        val oldRawValue2 = preferences.values.last()
        Prefs.changeObfuscator(fixture.newObfuscator)
        assertEquals("value", Prefs.get("key"))
        assertEquals("value2", Prefs.get(fixture.prefStringAlt))
        assertEquals(10, Prefs.get(fixture.prefIntDef))
        val newPreferences = Prefs.getAllObfuscated()
        val newRawKey = newPreferences.keys.first()
        val newRawValue = newPreferences.values.first()
        val newRawKey2 = newPreferences.keys.last()
        val newRawValue2 = newPreferences.values.last()
        assertNotEquals(oldRawKey, newRawKey)
        assertNotEquals("pref2", newRawKey)
        assertNotEquals(oldRawValue, newRawValue)
        assertNotEquals("10", newRawValue)
        assertNotEquals(oldRawKey2, newRawKey2)
        assertNotEquals("key", newRawKey2)
        assertNotEquals(oldRawValue2, newRawValue2)
        assertNotEquals("value", newRawValue2)
    }

    @Test
    fun observeIsCalledOnPutAndRemove() {
        val prefValues = mutableListOf<Any>()
        val f: (key: String, value: Any) -> Unit = { key, value -> prefValues.add(value) }
        Prefs.observe(f)
        Prefs.put("key", 1)
        Prefs.put("key", 3.0, "otherFile")
        Prefs.put("key", "value")
        Prefs.remove("key")
        fixture.prefStringAlt.put("new value")
        Prefs.remove(fixture.prefStringAlt)
        assertContentEquals(listOf(1, 3.0, "value", "", "new value", fixture.prefStringAlt.defaultValue), prefValues)
        Prefs.stopObserve(f)
        Prefs.put("key", 22)
        Prefs.put("key", 90)
        Prefs.put("key", BigDecimal.ZERO, "otherFile")
        Prefs.remove("key")
        fixture.prefStringAlt.put("new value")
        Prefs.remove(fixture.prefStringAlt)
        assertContentEquals(listOf(1, 3.0, "value", "", "new value", fixture.prefStringAlt.defaultValue), prefValues)
    }

    @Test
    fun newPref() {
        val prefFileName = fixture.altPrefFileName
        assertFailsWith<IllegalArgumentException> { Prefs.newPref("k", BigDecimal.ZERO) }
        assertPrefEquals(IPreference("k", 1, null), Prefs.newPref("k", 1))
        assertPrefEquals(IPreference("k", 2, prefFileName), Prefs.newPref("k", 2, prefFileName))
        assertPrefEquals(FPreference("k", 1F, null), Prefs.newPref("k", 1F))
        assertPrefEquals(FPreference("k", 2F, prefFileName), Prefs.newPref("k", 2F, prefFileName))
        assertPrefEquals(DPreference("k", 1.0, null), Prefs.newPref("k", 1.0))
        assertPrefEquals(DPreference("k", 2.0, prefFileName), Prefs.newPref("k", 2.0, prefFileName))
        assertPrefEquals(BPreference("k", true, null), Prefs.newPref("k", true))
        assertPrefEquals(BPreference("k", false, prefFileName), Prefs.newPref("k", false, prefFileName))
        assertPrefEquals(SPreference("k", "v", null), Prefs.newPref("k", "v"))
        assertPrefEquals(SPreference("k", "v2", prefFileName), Prefs.newPref("k", "v2", prefFileName))
        assertPrefEquals(LPreference("k", 1L, null), Prefs.newPref("k", 1L))
        assertPrefEquals(LPreference("k", 2L, prefFileName), Prefs.newPref("k", 2L, prefFileName))
        val bdPref = object : PowerfulPreference<BigDecimal>("k", BigDecimal.ZERO) {
            override fun parse(s: String): BigDecimal = BigDecimal.ONE
        }
        val bdPref2 = object : PowerfulPreference<BigDecimal>("k", BigDecimal.ZERO, prefFileName) {
            override fun parse(s: String): BigDecimal =
                if (s.isNotEmpty()) tryOr(BigDecimal.ONE) { BigDecimal(s) } else BigDecimal.ONE
        }
        assertPrefEquals(bdPref, Prefs.newPref("k", BigDecimal.ZERO, parse = { BigDecimal.ONE }))
        assertPrefEquals(
            bdPref2,
            Prefs.newPref("k", BigDecimal.ZERO, prefFileName, parse = { tryOr(BigDecimal.ONE) { BigDecimal(it) } })
        )
    }

    @Test
    fun newNullablePref() {
        val prefFileName = fixture.altPrefFileName
        assertFailsWith<IllegalArgumentException> { Prefs.newPref("k", BigDecimal.ZERO) }
        val bdPref = object : PowerfulPreference<BigDecimal>("k", BigDecimal.ZERO) {
            override fun parse(s: String): BigDecimal = BigDecimal.ONE
        }
        val bdPref2 = object : PowerfulPreference<BigDecimal>("k", BigDecimal.ZERO, prefFileName) {
            override fun parse(s: String): BigDecimal = tryOr(BigDecimal.ONE) { BigDecimal(s) }
        }
        assertPrefEquals(BPreferenceNullable("k", null, null), Prefs.newNullablePref(Boolean::class.java, "k", null))
        assertPrefEquals(
            BPreferenceNullable("k", false, prefFileName),
            Prefs.newNullablePref(Boolean::class.java, "k", false, prefFileName)
        )
        assertPrefEquals(IPreferenceNullable("k", null, null), Prefs.newNullablePref(Int::class.java, "k", null))
        assertPrefEquals(
            IPreferenceNullable("k", 5, prefFileName),
            Prefs.newNullablePref(Int::class.java, "k", 5, prefFileName)
        )
        assertPrefEquals(LPreferenceNullable("k", null, null), Prefs.newNullablePref(Long::class.java, "k", null))
        assertPrefEquals(
            LPreferenceNullable("k", 5, prefFileName),
            Prefs.newNullablePref(Long::class.java, "k", 5, prefFileName)
        )
        assertPrefEquals(DPreferenceNullable("k", null, null), Prefs.newNullablePref(Double::class.java, "k", null))
        assertPrefEquals(
            DPreferenceNullable("k", 5.0, prefFileName),
            Prefs.newNullablePref(Double::class.java, "k", 5.0, prefFileName)
        )
        assertPrefEquals(FPreferenceNullable("k", null, null), Prefs.newNullablePref(Float::class.java, "k", null))
        assertPrefEquals(
            FPreferenceNullable("k", 5F, prefFileName),
            Prefs.newNullablePref(Float::class.java, "k", 5F, prefFileName)
        )

        assertPrefEquals(
            bdPref,
            Prefs.newNullablePref(BigDecimal::class.java, "k", BigDecimal.ZERO, parse = { BigDecimal.ONE })
        )
        assertPrefEquals(
            bdPref2,
            Prefs.newNullablePref(
                BigDecimal::class.java,
                "k",
                BigDecimal.ZERO,
                prefFileName,
                parse = { tryOr(BigDecimal.ONE) { BigDecimal(it) } }
            )
        )
    }

    @Test
    fun newEnumPref() {
        val enumPref1 = EnumPreference("k", MyEnum.a, null)
        val enumPref2 = EnumPreference("k", MyEnum.a, fixture.altPrefFileName)
        assertPrefEquals(enumPref1, Prefs.newEnumPref("k", MyEnum.a))
        assertPrefEquals(enumPref2, Prefs.newEnumPref("k", MyEnum.a, fixture.altPrefFileName))
    }

    @Test
    fun getReturnsDefaultValueIfNotExists() {
        assertFalse(Prefs.contains(fixture.prefStringAlt))
        assertEquals(fixture.prefStringAlt.defaultValue, Prefs.get(fixture.prefStringAlt))
    }

    @Test
    fun putAndGet() {
        Prefs.put(fixture.prefStringAlt, "new value")
        assertEquals("new value", Prefs.get(fixture.prefStringAlt))
        Prefs.put(fixture.prefStringAlt.key, "new value2", fixture.prefStringAlt.preferencesFileName)
        assertEquals("new value2", Prefs.get(fixture.prefStringAlt))
        assertEquals("new value2", Prefs.get(fixture.prefStringAlt.key, fixture.prefStringAlt.preferencesFileName))
    }

    @Test
    fun remove() {
        assertFalse(Prefs.contains(fixture.prefStringAlt))
        fixture.prefStringAlt.put("new value")
        assertTrue(Prefs.contains(fixture.prefStringAlt))
        Prefs.remove(fixture.prefStringAlt)
        assertFalse(Prefs.contains(fixture.prefStringAlt))
        assertFalse(Prefs.contains("k"))
        Prefs.put("k", "new value")
        assertTrue(Prefs.contains("k"))
        Prefs.remove("k")
        assertFalse(Prefs.contains("k"))
    }

    @Test
    fun contains() {
        assertFalse(Prefs.contains(fixture.prefStringAlt))
        assertFalse(Prefs.contains(fixture.prefStringAlt.key, fixture.prefStringAlt.preferencesFileName))
        fixture.prefStringAlt.put("new value")
        assertTrue(Prefs.contains(fixture.prefStringAlt))
        assertTrue(Prefs.contains(fixture.prefStringAlt.key, fixture.prefStringAlt.preferencesFileName))

        assertFalse(Prefs.contains("k"))
        Prefs.put("k", "new value")
        assertTrue(Prefs.contains("k"))
    }

    @Test
    fun clear() {
        Prefs.put("k", "v")
        Prefs.put("k2", "v")
        Prefs.put("k", "v", fixture.altPrefFileName)
        Prefs.put("k2", "v", fixture.altPrefFileName)
        assertEquals(2, Prefs.getAll().size)
        assertEquals(2, Prefs.getAll(fixture.altPrefFileName).size)
        Prefs.clear()
        assertEquals(0, Prefs.getAll().size)
        assertEquals(2, Prefs.getAll(fixture.altPrefFileName).size)
        Prefs.clear(fixture.altPrefFileName)
        assertEquals(0, Prefs.getAll(fixture.altPrefFileName).size)
    }

    @Test
    fun getAllPreferenceFileNames() {
        val expected =
            listOf(context.packageName, fixture.plainPrefFileName, fixture.prefFileName, fixture.altPrefFileName)
        val returned = Prefs.getAllPreferenceFileNames()
        assertTrue(expected.containsAll(returned))
        assertTrue(returned.containsAll(expected))
    }

    @Test
    fun getAllPreferenceFileNamesReturnsPreferencesAddedBeforeAndAfterInit() {
        // fixture.altPrefFileName and "test" are not added during Prefs.init(), but we expect them to be found
        val expected =
            arrayListOf(context.packageName, fixture.plainPrefFileName, fixture.prefFileName, fixture.altPrefFileName)
        val returned = Prefs.getAllPreferenceFileNames()
        assertTrue(expected.containsAll(returned))
        assertTrue(returned.containsAll(expected))
        Prefs.put("k", "v", "test")
        expected.add("test")
        val returned2 = Prefs.getAllPreferenceFileNames()
        assertTrue(expected.containsAll(returned2))
        assertTrue(returned2.containsAll(expected))
    }

    @Test
    fun getAllObfuscatedReturnsObfuscatedKeysAndValues() {
        val c = fixture.oldObfuscator
        assertEquals(emptyMap<String, Any>(), Prefs.getAllObfuscated())
        fixture.prefIntDef.put(2)
        Prefs.put("k", "v")
        val obfuscated = Prefs.getAllObfuscated()
        assertEquals(2, obfuscated.size)
        assertNotEquals(setOf(fixture.prefIntDef.key, "k"), obfuscated.keys)
        assertEquals(setOf(c.obfuscate(fixture.prefIntDef.key), c.obfuscate("k")), obfuscated.keys)
        assertContentEquals(
            listOf(
                c.obfuscate(fixture.prefIntDef.toPreferences(2) + c.obfuscate(fixture.prefIntDef.key)),
                c.obfuscate("v" + c.obfuscate("k"))
            ),
            obfuscated.values
        )
    }

    @Test
    fun getAllObfuscatedReturnsUnobfuscatedKeysAndValuesForUnobfuscatedPreferences() {
        assertEquals(emptyMap<String, Any>(), Prefs.getAllObfuscated(fixture.plainPrefFileName))
        fixture.prefStringPlain.put("2")
        val obfuscated = Prefs.getAllObfuscated(fixture.plainPrefFileName)
        assertEquals(1, obfuscated.size)
        assertEquals(setOf(fixture.prefStringPlain.key), obfuscated.keys)
        assertContentEquals(listOf("2"), obfuscated.values)
    }

    @Test
    fun getAll() {
        assertEquals(emptyMap<String, Any>(), Prefs.getAll())
        fixture.prefIntDef.put(2)
        Prefs.put("k", "v")
        val obfuscated = Prefs.getAll()
        assertEquals(2, obfuscated.size)
        assertEquals(setOf(fixture.prefIntDef.key, "k"), obfuscated.keys)
        assertContentEquals(listOf("2", "v"), obfuscated.values)
    }

    private fun <A, R : PowerfulPreference<A>, T> assertPrefEquals(pref: R, pref2: PowerfulPreference<T>) = assertTrue(
        pref.getClassName() == pref2.getClassName() &&
            pref.key == pref2.key &&
            pref.defaultValue == pref2.defaultValue &&
            pref.preferencesFileName == pref2.preferencesFileName &&
            pref.toPreferences(pref.defaultValue) == pref2.toPreferences(pref2.defaultValue) &&
            pref.parse("") == pref2.parse("") &&
            pref.parse("false") == pref2.parse("false") &&
            pref.parse("true") == pref2.parse("true") &&
            pref.parse("1") == pref2.parse("1") &&
            pref.parse("test") == pref2.parse("test") &&
            pref.parse("6.23") == pref2.parse("6.23") &&
            pref.parse("null") == pref2.parse("null") &&
            pref.parse(pref.toPreferences(pref.defaultValue)) == pref2.parse(pref2.toPreferences(pref2.defaultValue))
    )
}
