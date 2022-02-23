package de.yard.threed.core.platform;

import de.yard.threed.core.StringUtils;
import de.yard.threed.core.Util;

public class PlatformHelper {

    public static Integer getIntSystemProperty(String property) {
        String arg = Platform.getInstance().getSystemProperty(property);
        if (arg == null) {
            return null;
        }
        if (StringUtils.empty(arg)) {
            return null;
        }
        return new Integer(Util.parseInt(arg));
    }

    /**
     */
    public static Boolean getBooleanSystemProperty(String property) {
        String arg = Platform.getInstance().getSystemProperty(property);
        if (arg == null) {
            return null;
        }
        if (Util.isTrue(arg)) {
            return true;
        }
        return false;
    }

    public static Double getDoubleSystemProperty(String property) {
        String arg = Platform.getInstance().getSystemProperty(property);
        if (arg == null) {
            return null;
        }
        if (StringUtils.empty(arg)) {
            return null;
        }
        return new Double(Util.parseDouble(arg));
    }

}
