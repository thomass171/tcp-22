package de.yard.threed.java2cs;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by thomass on 25.04.16.
 */
public class KnownGenericMethods {
    Map<String, List<String>> knowngenericmethods = new HashMap<String, List<String>>();

    public void add(String methodname, String[] types) {
        knowngenericmethods.put(methodname, Arrays.asList(types));
    }
}
