package de.yard.threed.core.configuration;

import java.util.HashMap;

public class Properties extends HashMap<String, String> {

    public Properties add(String key, String value) {
        put(key, value);
        return this;
    }
}
