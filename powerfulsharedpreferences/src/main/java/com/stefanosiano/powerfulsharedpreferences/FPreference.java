package com.stefanosiano.powerfulsharedpreferences;

/**
 * PowerfulPreference wrapper for Float
 */

class FPreference implements PowerfulPreference<Float> {

    private String key;
    private Float defaultValue;
    private String prefName;

    FPreference(String key, Float defaultValue, String prefName) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.prefName = prefName;
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

    @Override
    public String getPreferencesFileName() {
        return prefName;
    }
}
