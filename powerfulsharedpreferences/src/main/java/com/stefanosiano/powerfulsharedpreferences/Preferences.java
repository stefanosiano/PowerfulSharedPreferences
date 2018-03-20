package com.stefanosiano.powerfulsharedpreferences;

/**
 * PowerfulPreference wrapper for Double
 */

class BPreference extends PowerfulPreference<Boolean> {
    BPreference(String key, Boolean defaultValue, String prefName) {super(key, defaultValue, prefName);}
    @Override public Boolean parse(String s) {return Boolean.parseBoolean(s);}
    @Override public Class getPrefClass() {return Boolean.class;}
}
class IPreference extends PowerfulPreference<Integer> {
    IPreference(String key, Integer defaultValue, String prefName) {super(key, defaultValue, prefName);}
    @Override public Integer parse(String s) {return Integer.parseInt(s);}
    @Override public Class getPrefClass() {return Integer.class;}
}
class LPreference extends PowerfulPreference<Long> {
    LPreference(String key, Long defaultValue, String prefName) {super(key, defaultValue, prefName);}
    @Override public Long parse(String s) {return Long.parseLong(s);}
    @Override public Class getPrefClass() {return Long.class;}
}
class FPreference extends PowerfulPreference<Float> {
    FPreference(String key, Float defaultValue, String prefName) {super(key, defaultValue, prefName);}
    @Override public Float parse(String s) {return Float.parseFloat(s);}
    @Override public Class getPrefClass() {return Float.class;}
}
class DPreference extends PowerfulPreference<Double> {
    DPreference(String key, Double defaultValue, String prefName) {super(key, defaultValue, prefName);}
    @Override public Double parse(String s) {return Double.parseDouble(s);}
    @Override public Class getPrefClass() {return Double.class;}
}
class SPreference extends PowerfulPreference<String> {
    SPreference(String key, String defaultValue, String prefName) {super(key, defaultValue, prefName);}
    @Override public String parse(String s) {return s;}
    @Override public Class getPrefClass() {return String.class;}
}