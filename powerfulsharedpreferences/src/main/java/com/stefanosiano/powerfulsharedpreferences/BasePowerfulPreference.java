package com.stefanosiano.powerfulsharedpreferences;

/**
 * Base PowerfulPreference wrapper to be extended by other classes
 */

abstract class BasePowerfulPreference<T> implements PowerfulPreference<T> {

    private String key;
    private T defaultValue;
    private String prefName;

    BasePowerfulPreference(String key, T defaultValue, String prefName) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.prefName = prefName;
    }

    @Override
    public T getDefaultValue() {
        return defaultValue;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getPreferencesFileName() {
        return prefName;
    }

    @Override
    public T get() {
        return Prefs.get(this);
    }

    @Override
    public void put(T value) {
        Prefs.put(this, value);
    }
}
