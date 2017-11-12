package com.stefanosiano.powerfulsharedpreferences;

/**
 * PowerfulPreference wrapper for Float
 */

class FPreference extends PowerfulPreference<Float> {

    private String key;
    private Float defaultValue;

    FPreference(String key, Float defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    @Override
    public Float getDefaultValue() {
        return defaultValue;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public Float parse(String s) {
        return Float.parseFloat(s);
    }

    @Override
    public Class getPrefClass() {
        return Float.class;
    }
}
