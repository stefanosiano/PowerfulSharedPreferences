package com.stefanosiano.powerfulsharedpreferences;

/**
 * Created by stefano on 08/11/17.
 */

public interface PowerfulPreference<T> {
    T getDefaultValue();
    String getKey();
    T parse(String s);
}
