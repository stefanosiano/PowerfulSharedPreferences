package com.stefanosiano.powerful_libraries.sharedpreferencessample

/** Simple class to use in a preference. */
internal data class MyClass(val text: String) {
    override fun toString() = text
}
