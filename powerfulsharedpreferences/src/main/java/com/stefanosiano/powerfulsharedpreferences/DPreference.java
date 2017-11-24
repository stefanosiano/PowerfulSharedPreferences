package com.stefanosiano.powerfulsharedpreferences;

/**
 * PowerfulPreference wrapper for Double
 */

class DPreference implements PowerfulPreference<Double> {

    private String key;
    private Double defaultValue;
    private String prefName;

    DPreference(String key, Double defaultValue, String prefName) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.prefName = prefName;
    }

    @Override
    public Double getDefaultValue() {
        return defaultValue;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public Double parse(String s) {
        return Double.parseDouble(s);
    }

    @Override
    public Class getPrefClass() {
        return Double.class;
    }

    @Override
    public String getPreferencesFileName() {
        return prefName;
    }
}
