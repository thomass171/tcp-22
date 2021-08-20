package de.yard.threed.platform.webgl;


import de.yard.threed.core.platform.Log;

/**
 * Created by thomass on 20.04.15.
 * Der Logger ist der GWT Ersatz für javax.logging. Knofiguriert wird der in Gwt.gwt.xml.
 * 07.05.15: Der GWT Logger ist irgendwie problemtisch (auch wegen der Nachbildung von javax.logging (siehe deren Doku)
 * und wegen umstaendlichem Popuplogging.
 * Ich nehme einfach den javascript Logger.
 * 16.1.18: Der braucht aber viel Speicher ("About:" in Prozessliste).
 */
public class WebGlLog implements Log {
    String name, label;
    boolean enabled = true;//false;

    public WebGlLog(String name) {
        /*if (name==null){
            name="";
        }*/
        this.name = name;
        this.label = name;
        // StringUtils neither available yet nor required
        int len = name.length();
        if (len > 15) {
            label = name.substring(len - 15);
        }
    }

    public void debug(String msg) {
        if (!enabled)
            return;
        // kein Debug log in Production
        if (PlatformWebGl.isDevmode) {
            debugNative(buildMessage(msg));
        }
    }

    public void info(String msg) {
        if (!enabled)
            return;
        if (!PlatformWebGl.isDevmode) {
            logConsole(msg);
        } else {
            infoNative(buildMessage(msg));
        }
    }

    public void warn(String msg) {
        if (!enabled)
            return;
        if (!PlatformWebGl.isDevmode) {
            logConsole(msg);
        } else {
            warnNative(buildMessage(msg));
        }
    }

    public void error(String msg) {
        if (!PlatformWebGl.isDevmode) {
            logConsole(msg);
        } else {
            errorNative(buildMessage(msg));
        }
    }

    private String buildMessage(String msg) {
        return label + ":" + msg;
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

    private void logConsole(String msg) {
       logConsoleNative(msg);
    }

    private static native void debugNative(String msg) /*-{
        //MA34 $wnd.logger.debug(msg);
        console.debug(msg);
    }-*/;

    private static native void infoNative(String msg) /*-{
        //MA34$wnd.logger.info(msg);
        console.info(msg);
    }-*/;

    private static native void warnNative(String msg) /*-{
        //MA34$wnd.logger.warn(msg);
        console.warn(msg);
    }-*/;

    private static native void errorNative(String msg) /*-{
        //MA34$wnd.logger.error(msg);
        console.error(msg);
    }-*/;

    private static native void logConsoleNative( String msg ) /*-{
        console.log(msg);
    }-*/;
}
