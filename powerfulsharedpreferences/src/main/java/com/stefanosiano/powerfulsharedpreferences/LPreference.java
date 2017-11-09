package com.stefanosiano.powerfulsharedpreferences;

/**
 * PowerfulPreference wrapper for Long
 */

class LPreference extends PowerfulPreference<Long> {

    private String key;
    private Long defaultValue;

    LPreference(String key, Long defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
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
}
