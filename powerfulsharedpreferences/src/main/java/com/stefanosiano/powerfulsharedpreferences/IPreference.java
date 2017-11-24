package com.stefanosiano.powerfulsharedpreferences;

/**
 * PowerfulPreference wrapper for Integer
 */

class IPreference implements PowerfulPreference<Integer> {

    private String key;
    private Integer defaultValue;
    private String prefName;

    IPreference(String key, Integer defaultValue, String prefName) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.prefName = prefName;
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

    @Override
    public String getPreferencesFileName() {
        return prefName;
    }
}
