package com.stefanosiano.powerfulsharedpreferences;

/**
 * PowerfulPreference wrapper for Double
 */

class DPreference extends PowerfulPreference<Double> {

    private String key;
    private Double defaultValue;

    DPreference(String key, Double defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
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
}
