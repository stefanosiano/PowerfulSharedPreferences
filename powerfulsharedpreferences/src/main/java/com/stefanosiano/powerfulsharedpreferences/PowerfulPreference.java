package com.stefanosiano.powerfulsharedpreferences;

/**
 * PowerfulPreference object used to retrieve information to save and retrieve data into SharedPreferences
 */

public interface PowerfulPreference<T> {

    /** Returns the file name associated to this preference */
    String getPreferencesFileName();

    /** Returns the default value of the preference */
    T getDefaultValue();

    /** Returns the key of the preference */
    String getKey();

    /** Returns the class of the value to save/retrieve */
    Class getPrefClass();

    /** Returns the data of the preference from a string */
    T parse(String s) throws Exception;
}
