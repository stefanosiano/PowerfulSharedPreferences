package com.stefanosiano.powerful_libraries.sharedpreferences

import android.content.Context
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

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
    fun flowContainsSetValue() {
        fixture.prefString.put("my value")
        assertEquals(fixture.prefString.asFlow().value, "my value")

    }

    @Test
    fun flowCallbackCalledOnlyOnce() {
        var changes = 0
        val flow1 = fixture.prefString.asFlow()
        val flow2 = fixture.prefString.asFlow()
        assertSame(flow1, flow2)

        GlobalScope.launch {
            flow1.onEach {
                changes++
            }.collect()
        }
        assertEquals(flow1.value, "default value")
        assertEquals(flow2.value, "default value")
        fixture.prefString.put("new value")
        assertEquals(flow1.value, "new value")
        assertEquals(flow2.value, "new value")
        // This should be ignored, as the value didn't change
        fixture.prefString.put("new value")

        var waitCycles = 0
        // sleep at most 3 times to let the flow update the value on background. Will fail if it doesn't
        while (changes == 0 && waitCycles < 3) {
            waitCycles++
            Thread.sleep(100)
        }
        assertEquals(changes, 1)
    }

    @Test
    fun getCacheMapKey() {
        assertEquals("pref name 1\$pref1", fixture.prefString.getCacheMapKey())
        assertEquals("null\$pref2", fixture.prefInt.getCacheMapKey())
    }

    @Test
    fun getAndPut() {
        assertEquals("", Prefs[fixture.prefString.key, fixture.prefString.preferencesFileName])
        assertEquals("default value", Prefs[fixture.prefString])
        assertEquals("default value", fixture.prefString.get())
        assertEquals(4, fixture.prefInt.get())
        Prefs.put(fixture.prefString, "first")
        assertEquals("first", Prefs[fixture.prefString.key, fixture.prefString.preferencesFileName])
        assertEquals("first", Prefs[fixture.prefString])
        assertEquals("first", fixture.prefString.get())
        assertEquals(4, fixture.prefInt.get())
        Prefs.put(fixture.prefString.key, "second", fixture.prefString.preferencesFileName)
        Prefs.put(fixture.prefInt.key, 12, fixture.prefInt.preferencesFileName)
        assertEquals("second", Prefs[fixture.prefString.key, fixture.prefString.preferencesFileName])
        assertEquals("second", Prefs[fixture.prefString])
        assertEquals("second", fixture.prefString.get())
        assertEquals(12, fixture.prefInt.get())
        Prefs.put(fixture.prefInt.key, 4)
        assertEquals("4", Prefs[fixture.prefInt.key, fixture.prefInt.preferencesFileName])
        assertEquals(4, Prefs[fixture.prefInt])
        assertEquals(4, fixture.prefInt.get())
    }
}
