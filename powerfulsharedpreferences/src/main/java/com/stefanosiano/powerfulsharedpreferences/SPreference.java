package com.stefanosiano.powerfulsharedpreferences;

/**
 * PowerfulPreference wrapper for String
 */

class SPreference extends PowerfulPreference<String> {

    private String key;
    private String defaultValue;

    SPreference(String key, String defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
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
}
