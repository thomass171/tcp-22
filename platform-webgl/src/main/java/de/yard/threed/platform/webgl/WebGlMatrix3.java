package de.yard.threed.platform.webgl;

import com.google.gwt.core.client.JavaScriptObject;
import de.yard.threed.core.Matrix3;
import de.yard.threed.core.Matrix4;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;


/**
 * No setter like in Matrix4
 */
public class WebGlMatrix3 {
    JavaScriptObject matrix3;

    public WebGlMatrix3() {
        matrix3 = buildMatrix3();
    }

    public WebGlMatrix3(double a11, double a12, double a13,
                        double a21, double a22, double a23,
                        double a31, double a32, double a33) {
        matrix3 = buildMatrix3(a11, a12, a13,
                a21, a22, a23,
                a31, a32, a33);
    }

    public static WebGlMatrix3 toWebGl(Matrix3 m) {
        return new WebGlMatrix3(
                m.e11, m.e12, m.e13,
                m.e21, m.e22, m.e23,
                m.e31, m.e32, m.e33);
    }

    WebGlMatrix3(JavaScriptObject matrix3) {
        this.matrix3 = matrix3;
    }

    public double getElement(int row, int column) {
        return getElement(matrix3, row, column);
    }

    private static native JavaScriptObject buildMatrix3()  /*-{
        return new $wnd.THREE.Matrix3( );
    }-*/;

    private static native JavaScriptObject buildMatrix3(double a11, double a12, double a13,
                                                        double a21, double a22, double a23,
                                                        double a31, double a32, double a33)  /*-{
        var v = new $wnd.THREE.Matrix3( );
        v.set( a11,  a12,  a13,
                    a21,  a22,  a23,
                    a31,  a32,  a33);
        return v;
    }-*/;

    private static native JavaScriptObject multiply(JavaScriptObject m1, JavaScriptObject m2)  /*-{
        var v = new $wnd.THREE.Matrix4();
        v.multiplyMatrices(m1,m2);
        return v;
    }-*/;

    private static native double getElement(JavaScriptObject m, int row, int column)  /*-{
        // ThrreJs hat offenbar row und columns getauscht im array.
        return m.elements[row+column*3];
    }-*/;

}
