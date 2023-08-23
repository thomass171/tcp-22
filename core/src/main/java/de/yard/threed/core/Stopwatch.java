package de.yard.threed.core;

import de.yard.threed.core.platform.Platform;

import java.util.HashMap;
import java.util.Map;

public class Stopwatch {
    long startTime, latest;
    Map<String, String> tags = new HashMap<String, String>();

    public Stopwatch() {
        startTime = Platform.getInstance().currentTimeMillis();
        latest = startTime;
    }

    public void tag(String tag) {
        tags.put(tag, "" + (Platform.getInstance().currentTimeMillis() - latest));
        latest = Platform.getInstance().currentTimeMillis();
    }

    public String report() {
        long current = Platform.getInstance().currentTimeMillis();
        String result = "";
        for (String tag : tags.keySet()) {
            if (StringUtils.length(result) > 0) {
                result += ", ";
            }
            result += tag + " " + tags.get(tag) + " ms";
        }
        if (StringUtils.length(result) > 0) {
            result += ", ";
        }
        result += " total " + (current - startTime) + " ms";
        return result;
    }

}
