package com.stefanosiano.powerfulsharedpreferences;

/**
 * PowerfulPreference wrapper for Integer
 */

class IPreference extends PowerfulPreference<Integer> {

    private String key;
    private Integer defaultValue;

    IPreference(String key, Integer defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    @Override
    public Integer getDefaultValue() {
        return defaultValue;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public Integer parse(String s) {
        return Integer.parseInt(s);
    }

    @Override
    public Class getPrefClass() {
        return Integer.class;
    }
}
