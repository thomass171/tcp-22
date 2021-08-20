package de.yard.threed.platform.webgl;

import com.google.gwt.core.client.JavaScriptObject;
import de.yard.threed.core.Matrix4;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;




/**
 * Created by thomass on 25.04.15.
 * <p/>
 * Auch hier erstmal keine setter
 */
public class WebGlMatrix4 /*implements Matrix4*/ {
    JavaScriptObject matrix4;

    public WebGlMatrix4(double a11, double a12, double a13, double a14,
                        double a21, double a22, double a23, double a24,
                        double a31, double a32, double a33, double a34,
                        double a41, double a42, double a43, double a44) {
        matrix4 = buildMatrix4(a11, a12, a13, a14,
                a21, a22, a23, a24,
                a31, a32, a33, a34,
                a41, a42, a43, a44);
    }

    public WebGlMatrix4(WebGlQuaternion quaternion) {
        matrix4 = buildRotationMatrix4FromQuaternion(quaternion.quaternion);
    }

    public static WebGlMatrix4 toWebGl(Matrix4 m) {
        return  new WebGlMatrix4(
                m.a11,m.a12,m.a13,m.a14,
                m.a21,m.a22,m.a23,m.a24,
                m.a31,m.a32,m.a33,m.a34,
                m.a41,m.a42,m.a43,m.a44           );
    }

    public static Matrix4 fromWebGl(WebGlMatrix4 m) {
        return  new Matrix4(
                m.getElement(0,0),m.getElement(0,1),m.getElement(0,2),m.getElement(0,3),
                m.getElement(1,0),m.getElement(1,1),m.getElement(1,2),m.getElement(1,3),
                m.getElement(2,0),m.getElement(2,1),m.getElement(2,2),m.getElement(2,3),
                m.getElement(3,0),m.getElement(3,1),m.getElement(3,2),m.getElement(3,3)           );
    }

    WebGlMatrix4(JavaScriptObject matrix4) {
        this.matrix4 = matrix4;
    }

    //@Override
    public Vector3 transform(Vector3 v) {
        return WebGlVector3.fromWebGl(new WebGlVector3(transform(WebGlVector3.toWebGl(v).vector3,matrix4)));
    }

    //@Override
    public Matrix4 multiply(Matrix4 m) {
        return WebGlMatrix4.fromWebGl(new WebGlMatrix4(multiply(matrix4,WebGlMatrix4.toWebGl(m).matrix4)));
    }

    //@Override
    public double getElement(int row, int column){
        return getElement(matrix4,row,column);
    }

    //@Override
    public Quaternion extractQuaternion() {
        return WebGlQuaternion.fromWebGl(new WebGlQuaternion(extractQuaternion(matrix4)));
    }

    private static native JavaScriptObject buildMatrix4(double a11, double a12, double a13, double a14,
                                                              double a21, double a22, double a23, double a24,
                                                              double a31, double a32, double a33, double a34,
                                                              double a41, double a42, double a43, double a44)  /*-{
        // In Thrrejs 71 kann man keine Parameter mehr an den Konstruktor uebergeben
        var v = new $wnd.THREE.Matrix4( );
        v.set( a11,  a12,  a13,  a14,
                    a21,  a22,  a23,  a24,
                    a31,  a32,  a33,  a34,
                    a41,  a42,  a43,  a44);        
        return v;
    }-*/;

    private static native JavaScriptObject buildRotationMatrix4FromQuaternion(JavaScriptObject q)  /*-{
        var v = new $wnd.THREE.Matrix4();
        v.makeRotationFromQuaternion(q);
        return v;
    }-*/;

    private static native JavaScriptObject transform(JavaScriptObject vector3, JavaScriptObject matrix4)  /*-{
        var v = vector3.clone();
        v.applyMatrix4(matrix4);
        return v;
    }-*/;

    private static native JavaScriptObject multiply(JavaScriptObject m1, JavaScriptObject m2)  /*-{
        var v = new $wnd.THREE.Matrix4();
        v.multiplyMatrices(m1,m2);
        return v;
    }-*/;

    private static native double getElement(JavaScriptObject m, int row, int column)  /*-{
        // ThrreJs hat offenbar row und columns getauscht im array.
        return m.elements[row+column*4];
    }-*/;

    private static native JavaScriptObject extractQuaternion(JavaScriptObject m)  /*-{
        var position = new $wnd.THREE.Vector3();
        var quaternion = new $wnd.THREE.Quaternion();
        var scale = new $wnd.THREE.Vector3();
        m.decompose(position, quaternion, scale);
        return quaternion;
    }-*/;

}
