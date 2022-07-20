package com.stefanosiano.powerful_libraries.sharedpreferences

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

internal class PrefContainerTest : BaseTest() {

    private val prefContainer = PrefContainer(false, "name", 0)

    @Test
    fun build() {
        assertNull(prefContainer.sharedPreferences)
        prefContainer.build(context)
        assertNotNull(prefContainer.sharedPreferences)
        assertEquals(prefContainer.sharedPreferences, context.applicationContext.getSharedPreferences("name", 0))
    }
}
