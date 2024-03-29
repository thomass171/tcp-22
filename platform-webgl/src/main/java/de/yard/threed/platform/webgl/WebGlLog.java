package de.yard.threed.platform.webgl;


import com.google.gwt.i18n.client.DateTimeFormat;
import de.yard.threed.core.platform.Log;

import java.util.Date;

/**
 * A very simple logger instead of the problematic GWT logger or similar cumbersome popup loggings.
 * <p>
 * Created by thomass on 20.04.15.
 */
public class WebGlLog implements Log {
    String label;
    private boolean enabled = true;//false;

    public WebGlLog(String name) {
        this.label = name;
        // StringUtils neither available yet nor required
        int len = name.length();
        if (len > 15) {
            label = name.substring(len - 15);
        }
    }

    public void trace(String msg) {
        if (!enabled)
            return;
        // Log level is handled by LevelLogFactory wrapper, so this isn't reached when trace isn't enabled.
        debugNative(buildMessage(msg));
    }

    public void debug(String msg) {
        if (!enabled)
            return;
        debugNative(buildMessage(msg));
    }

    public void info(String msg) {
        if (!enabled)
            return;
        infoNative(buildMessage(msg));
    }

    public void warn(String msg) {
        if (!enabled)
            return;
        warnNative(buildMessage(msg));
    }

    public void error(String msg) {
        errorNative(buildMessage(msg));
    }

    public static void logNative(String msg) {
        logConsoleNative(msg);
    }

    private String buildMessage(String msg) {
        String s = DateTimeFormat.getFormat("HH:mm:ss").format(new Date());
        return label + ":" + s + " " + msg;
    }

    @Override
    public void error(String msg, Exception e) {
        e.printStackTrace();
        error(msg + e.getMessage());
    }

    @Override
    public void warn(String msg, Exception e) {
        e.printStackTrace();
        error(msg + e.getMessage());
    }

    private static native void debugNative(String msg) /*-{
        console.debug(msg);
    }-*/;

    private static native void infoNative(String msg) /*-{
        console.info(msg);
    }-*/;

    private static native void warnNative(String msg) /*-{
        console.warn(msg);
    }-*/;

    private static native void errorNative(String msg) /*-{
        console.error(msg);
    }-*/;

    private static native void logConsoleNative(String msg) /*-{
        console.log(msg);
    }-*/;
}
