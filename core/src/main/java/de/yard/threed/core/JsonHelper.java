package de.yard.threed.core;

import de.yard.threed.core.platform.NativeJsonNumber;
import de.yard.threed.core.platform.NativeJsonObject;
import de.yard.threed.core.platform.NativeJsonString;
import de.yard.threed.core.platform.NativeJsonValue;

public class JsonHelper {

    public static String getString(NativeJsonObject json, String key) {
        NativeJsonValue v = json.get(key);
        if (v == null) {
            return null;
        }
        NativeJsonString n = v.isString();
        if (n == null) {
            return null;
        }
        return n.stringValue();
    }

    public static Double getDouble(NativeJsonObject json, String key) {
        NativeJsonValue v = json.get(key);
        if (v == null) {
            return null;
        }
        NativeJsonNumber n = v.isNumber();
        if (n == null) {
            return null;
        }
        return new Double(n.doubleValue());
    }

    public static Integer getInt(NativeJsonObject json, String key) {
        NativeJsonValue v = json.get(key);
        if (v == null) {
            return null;
        }
        NativeJsonNumber n = v.isNumber();
        if (n == null) {
            return null;
        }
        return new Integer(n.intValue());
    }

    public static String buildProperty(String tag, String v) {
        String result = "\"" + tag + "\":";

        //OhOhOH
        v = v.replace("\"", "\\\"");
        //// GWT JsonParser doesn't like line breaks.
        //??v = v.replace("\\n", "\\\n");
        return result + "\"" + v + "\"";
    }

    public static String buildProperty(String tag, double v) {
        String result = "\"" + tag + "\":";

        return result + v;
    }
}
