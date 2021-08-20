package de.yard.threed.platform.webgl;

import com.google.gwt.core.client.JavaScriptObject;


/**
 * Created by thomass on 18.08.15.
 *
 * Auch hier erstmal keine setter
 */
public class WebGlVector2  {
    JavaScriptObject vector2;

    public WebGlVector2(double x, double y) {
        vector2 = buildNativeVector2(x,y);
    }

    WebGlVector2(JavaScriptObject vector2) {
        this.vector2 = vector2;
    }

    public double getX() {
        return (getX(vector2));
    }

    public double getY() {
        return (getY(vector2));
    }


    private static native JavaScriptObject buildNativeVector2(double x, double y)  /*-{
        var v = new $wnd.THREE.Vector2(x,y);
        return v;
    }-*/;

    private static native double getX(JavaScriptObject vector2)  /*-{
        return vector2.x;
    }-*/;

    private static native double getY(JavaScriptObject vector2)  /*-{
        return vector2.y;
    }-*/;



}
