package com.stefanosiano.powerfulsharedpreferences;

/**
 * PowerfulPreference object used to retrieve information to save and retrieve data into SharedPreferences
 */

public abstract class PowerfulPreference<T> {

    /** Returns the default value of the preference */
    public abstract T getDefaultValue();

    /** Returns the key of the preference */
    public abstract String getKey();

    /** Returns the class of the value to save/retrieve */
    abstract Class getPrefClass();

    /** Returns the data of the preference parsing a string */
    abstract T parse(String s) throws Exception;
}
