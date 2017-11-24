package com.stefanosiano.powerfulsharedpreferences;

/**
 * PowerfulPreference wrapper for Boolean
 */

class BPreference implements PowerfulPreference<Boolean> {

    private String key;
    private Boolean defaultValue;
    private String prefName;

    BPreference(String key, Boolean defaultValue, String prefName) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.prefName = prefName;
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

    @Override
    public String getPreferencesFileName() {
        return prefName;
    }
}
