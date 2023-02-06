package de.yard.threed.core.platform;

import de.yard.threed.core.Util;

/**
 * 2.8.21:TODO move to core
 */
public class Config {

    private static boolean asyncdebuglog = false;
    public static boolean loaderdebuglog = false;
    public static boolean modelloaddebuglog = false;
    public static boolean terrainloaddebuglog = false;
    public static boolean materiallibdebuglog = false;
    public static boolean animationdebuglog = false;

    public static void initFromArguments() {
        String arg = Platform.getInstance().getConfiguration().getString("argv.config.asyncdebuglog");
        if (arg != null) {
            asyncdebuglog = Util.isTrue(arg);
        }

    }

    public static boolean isAsyncdebuglog() {
        return asyncdebuglog;
    }
}
