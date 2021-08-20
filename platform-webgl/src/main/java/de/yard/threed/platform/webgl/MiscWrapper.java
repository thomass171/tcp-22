package de.yard.threed.platform.webgl;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Created by thomass on 25.04.15.
 */
public class MiscWrapper {
    public static native void alert(String msg)  /*-{
        $wnd.alert(msg);
    }-*/;

    /**
     * Geht zumindest Firefox wohl nicht so?
     */
    public static native void printStackTrace()  /*-{
        console.trace();
    }-*/;

    public static native void print(String msg)  /*-{
        console.log(msg);
    }-*/;
}
