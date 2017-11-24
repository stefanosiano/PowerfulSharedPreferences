package com.stefanosiano.powerfulsharedpreferences;

/**
 * PowerfulPreference wrapper for Long
 */

class LPreference implements PowerfulPreference<Long> {

    private String key;
    private Long defaultValue;
    private String prefName;

    LPreference(String key, Long defaultValue, String prefName) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.prefName = prefName;
    }

    @Override
    public Long getDefaultValue() {
        return defaultValue;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public Long parse(String s) {
        return Long.parseLong(s);
    }

    @Override
    public Class getPrefClass() {
        return Long.class;
    }

    @Override
    public String getPreferencesFileName() {
        return prefName;
    }
}
