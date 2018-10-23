package com.stefanosiano.powerfulsharedpreferences;

/**
 * PowerfulPreference object used to retrieve information to save and retrieve data into SharedPreferences
 */

public abstract class PowerfulPreference<T> {

    private String key;
    private T defaultValue;
    private String prefName;

    protected PowerfulPreference(String key, T defaultValue, String prefName) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.prefName = prefName;
    }

    protected PowerfulPreference(String key, T defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.prefName = null;
    }

    /** Returns the file name associated to this preference */
    public String getPreferencesFileName() {
        return prefName;
    }

    /** Returns the default value of the preference */
    public T getDefaultValue() {
        return defaultValue;
    }

    /** Returns the key of the preference */
    public String getKey() {
        return key;
    }

    /** Returns the key of the cache map of the preferences */
    public String getCacheMapKey() {
        return prefName + "$" + key;
    }

    /** Returns the class of the value to save/retrieve */
    protected abstract Class getPrefClass();

    /** Returns the data of the preference from a string. Exceptions are handled by the library itself */
    protected abstract T parse(String s);


    /** Returns the value of this preference */
    public T get() {
        return Prefs.get(this);
    }

    /** Puts a value to this preference */
    public void put(T value) {
        Prefs.put(this, value);
    }
}
