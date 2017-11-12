package com.stefanosiano.powerfulsharedpreferences;

/**
 * PowerfulPreference wrapper for Boolean
 */

class BPreference extends PowerfulPreference<Boolean> {

    private String key;
    private Boolean defaultValue;

    BPreference(String key, Boolean defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    @Override
    public Boolean getDefaultValue() {
        return defaultValue;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public Boolean parse(String s) {
        return Boolean.parseBoolean(s);
    }

    @Override
    public Class getPrefClass() {
        return Boolean.class;
    }
}
