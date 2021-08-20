package de.yard.threed.platform.webgl;

import com.google.gwt.core.client.JavaScriptObject;
import de.yard.threed.core.Vector3;


/**
 * Created by thomass on 25.04.15.
 * <p>
 * Auch hier erstmal keine setter
 */
public class WebGlVector3 /*implements Vector3*/ {
    JavaScriptObject vector3;

    public WebGlVector3(double x, double y, double z) {
        vector3 = buildVector3(x, y, z);
    }

    WebGlVector3(JavaScriptObject vector3) {
        this.vector3 = vector3;
    }

    public static WebGlVector3 toWebGl(Vector3 v) {
        return new WebGlVector3(v.getX(), v.getY(), v.getZ());
    }

    public static Vector3 fromWebGl(WebGlVector3 v) {
        return new Vector3(v.getX(), v.getY(), v.getZ());
    }

    public double getX() {
        return (getX(vector3));
    }

    public double getY() {
        return (getY(vector3));
    }

    public double getZ() {
        return (getZ(vector3));
    }

    private static native JavaScriptObject buildVector3(double x, double y, double z)  /*-{
        var v = new $wnd.THREE.Vector3(x,y,z);
        return v;
    }-*/;

    private static native void rotate(JavaScriptObject object3d, double angle)  /*-{
        object3d.rotateX(angle);
    }-*/;

    private static native double getX(JavaScriptObject vector3)  /*-{
        return vector3.x;
    }-*/;

    private static native double getY(JavaScriptObject vector3)  /*-{
        return vector3.y;
    }-*/;

    private static native double getZ(JavaScriptObject vector3)  /*-{
        return vector3.z;
    }-*/;


}
