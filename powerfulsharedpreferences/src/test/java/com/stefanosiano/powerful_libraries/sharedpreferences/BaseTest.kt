package com.stefanosiano.powerful_libraries.sharedpreferences

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

@RunWith(AndroidJUnit4::class)
@Suppress("UnnecessaryAbstractClass")
abstract class BaseTest {
    protected lateinit var context: Context

    @BeforeTest
    fun `set up`() {
        context = ApplicationProvider.getApplicationContext()
    }

    @AfterTest
    fun clearPrefs() {
        Prefs.clearForTests()
    }
}
