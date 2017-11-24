package com.stefanosiano.powerfulsharedpreferences;

/**
 * PowerfulPreference wrapper for String
 */

class SPreference implements PowerfulPreference<String> {

    private String key;
    private String defaultValue;
    private String prefName;

    SPreference(String key, String defaultValue, String prefName) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.prefName = prefName;
    }

    @Override
    public String getDefaultValue() {
        return defaultValue;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String parse(String s) {
        return s;
    }

    @Override
    public Class getPrefClass() {
        return String.class;
    }

    @Override
    public String getPreferencesFileName() {
        return prefName;
    }
}
