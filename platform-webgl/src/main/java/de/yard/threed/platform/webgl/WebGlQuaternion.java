package de.yard.threed.platform.webgl;

import com.google.gwt.core.client.JavaScriptObject;
import de.yard.threed.core.Matrix4;
import de.yard.threed.core.Quaternion;


/**
 * Created by thomass on 25.04.15.
 * <p>
 * Auch hier erstmal keine setter
 */
public class WebGlQuaternion /*implements Quaternion*/ {
    JavaScriptObject quaternion;

    public WebGlQuaternion(double x, double y, double z, double w) {
        quaternion = buildQuaternion(x, y, z, w);
    }

    WebGlQuaternion(JavaScriptObject q) {
        this.quaternion = q;
    }

    public static WebGlQuaternion toWebGl(Quaternion v) {
        return new WebGlQuaternion(v.getX(), v.getY(), v.getZ(), v.getW());
    }

    public static Quaternion fromWebGl(WebGlQuaternion v) {
        return new Quaternion(v.getX(), v.getY(), v.getZ(), v.getW());
    }

    public double getX() {
        return (getX(quaternion));
    }

    public double getY() {
        return (getY(quaternion));
    }

    public double getZ() {
        return (getZ(quaternion));
    }

    public double getW() {
        return (getW(quaternion));
    }

    public void normalize() {
        normalize(quaternion);
    }

    public Matrix4 buildRotationMatrix() {
        return WebGlMatrix4.fromWebGl(new WebGlMatrix4(buildRotationMatrix(quaternion)));
    }

    private static native JavaScriptObject buildQuaternion(double x, double y, double z, double w)  /*-{
        var v = new $wnd.THREE.Quaternion(x,y,z,w);
        return v;
    }-*/;

    private static native double getX(JavaScriptObject quaternion)  /*-{
        return quaternion.x;
    }-*/;

    private static native double getY(JavaScriptObject quaternion)  /*-{
        return quaternion.y;
    }-*/;

    private static native double getZ(JavaScriptObject quaternion)  /*-{
        return quaternion.z;
    }-*/;

    private static native double getW(JavaScriptObject quaternion)  /*-{
        return quaternion.w;
    }-*/;

    private static native double normalize(JavaScriptObject quaternion)  /*-{
        quaternion.normalize();
    }-*/;

    private static native JavaScriptObject buildRotationMatrix(JavaScriptObject quaternion)  /*-{
        var m = new $wnd.THREE.Matrix4();
        m.makeRotationFromQuaternion(quaternion);
        return m;
    }-*/;

}
