package com.stefanosiano.powerful_libraries.sharedpreferences

import android.content.Context
import org.junit.Test
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

class PowerfulPreferenceTest : BaseTest() {

    private class Fixture {
        val prefString = SPreference("pref1", "default value", "pref name 1")
        val prefInt = IPreference("pref2", 4, null)
    }

    private lateinit var fixture: Fixture

    @BeforeTest
    fun initPrefs() {
        Prefs
            .init(context)
            .addPrefs("pref name 1", Context.MODE_PRIVATE, true)
            .build()
        fixture = Fixture()
    }

    @Test
    fun observe() {
        var changes = 0
        var sValue = ""
        var iValue = 0
        fixture.prefString.observe {
            changes++
            sValue = it
        }
        fixture.prefInt.observe {
            changes++
            iValue = it
        }
        fixture.prefInt.observe {
            changes++
            iValue = it * 2
        }
        fixture.prefString.put("first change")
        fixture.prefInt.put(50)
        assertEquals(3, changes)
        assertEquals("first change", sValue)
        assertEquals(100, iValue)
    }

    @Test
    fun stopObserve() {
        var changes = 0
        val sF: (value: String) -> Unit = { _ -> changes++ }
        val iF: (value: Int) -> Unit = { _ -> changes++ }

        fixture.prefString.observe(sF)
        fixture.prefInt.observe(iF)
        fixture.prefString.put("first change")
        fixture.prefInt.put(50)
        assertEquals(2, changes)
        fixture.prefString.stopObserve(sF)
        fixture.prefInt.stopObserve(iF)
        fixture.prefString.put("second change")
        fixture.prefInt.put(12)
        assertEquals(2, changes)
    }

    @Test
    fun callOnChange() {
        var changes = 0
        var sValue = ""
        val sF: (value: String) -> Unit = { s ->
            changes++
            sValue = s
        }
        val iF: (value: Int) -> Unit = { _ -> changes++ }

        fixture.prefString.observe(sF)
        fixture.prefInt.observe(iF)
        fixture.prefString.callOnChange()
        fixture.prefInt.callOnChange()
        assertEquals(2, changes)
        assertEquals("default value", sValue)
        fixture.prefString.callOnChange("change")
        assertEquals(3, changes)
        assertEquals("change", sValue)
    }

    @Test
    fun getCacheMapKey() {
        assertEquals("pref name 1\$pref1", fixture.prefString.getCacheMapKey())
        assertEquals("null\$pref2", fixture.prefInt.getCacheMapKey())
    }

    @Test
    fun getAndPut() {
        assertEquals("", Prefs.get(fixture.prefString.key, fixture.prefString.preferencesFileName))
        assertEquals("default value", Prefs.get(fixture.prefString))
        assertEquals("default value", fixture.prefString.get())
        assertEquals(4, fixture.prefInt.get())
        Prefs.put(fixture.prefString, "first")
        assertEquals("first", Prefs.get(fixture.prefString.key, fixture.prefString.preferencesFileName))
        assertEquals("first", Prefs.get(fixture.prefString))
        assertEquals("first", fixture.prefString.get())
        assertEquals(4, fixture.prefInt.get())
        Prefs.put(fixture.prefString.key, "second", fixture.prefString.preferencesFileName)
        Prefs.put(fixture.prefInt.key, 12, fixture.prefInt.preferencesFileName)
        assertEquals("second", Prefs.get(fixture.prefString.key, fixture.prefString.preferencesFileName))
        assertEquals("second", Prefs.get(fixture.prefString))
        assertEquals("second", fixture.prefString.get())
        assertEquals(12, fixture.prefInt.get())
        Prefs.put(fixture.prefInt.key, 4)
        assertEquals("4", Prefs.get(fixture.prefInt.key, fixture.prefInt.preferencesFileName))
        assertEquals(4, Prefs.get(fixture.prefInt))
        assertEquals(4, fixture.prefInt.get())
    }
}
