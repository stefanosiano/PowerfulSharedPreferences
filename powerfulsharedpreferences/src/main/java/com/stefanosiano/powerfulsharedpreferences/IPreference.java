package com.stefanosiano.powerfulsharedpreferences;

/**
 * Created by stefano on 08/11/17.
 */

public class IPreference implements PowerfulPreference<Integer> {

    String key;
    Integer defaultValue;

    public IPreference(String key, Integer defaultValue) {
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
}
