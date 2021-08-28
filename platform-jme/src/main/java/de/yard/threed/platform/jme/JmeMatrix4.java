package de.yard.threed.platform.jme;

import com.jme3.math.Matrix4f;


import de.yard.threed.core.Matrix4;

/**
 * Created by thomass on 05.06.15.
 */
public class JmeMatrix4 /*implements Matrix4*/ {
    Matrix4f matrix4;

    public JmeMatrix4(float a11, float a12, float a13, float a14,
                        float a21, float a22, float a23, float a24,
                        float a31, float a32, float a33, float a34,
                        float a41, float a42, float a43, float a44) {
        matrix4 = new Matrix4f(a11, a12, a13, a14,
                a21, a22, a23, a24,
                a31, a32, a33, a34,
                a41, a42, a43, a44);
    }

    public static Matrix4f toJme(Matrix4 m) {
        return  new Matrix4f(
                (float)m.a11,(float)m.a12,(float)m.a13,(float)m.a14,
                (float)m.a21,(float)m.a22,(float)m.a23,(float)m.a24,
                (float)m.a31,(float)m.a32,(float)m.a33,(float)m.a34,
                (float)m.a41,(float)m.a42,(float)m.a43,(float)m.a44           );
    }

    public static Matrix4 fromJme(Matrix4f m) {
        return  new Matrix4(
                (float)m.m00,(float)m.m01,(float)m.m02,(float)m.m03,
                (float)m.m10,(float)m.m11,(float)m.m12,(float)m.m13,
                (float)m.m20,(float)m.m21,(float)m.m22,(float)m.m23,
                (float)m.m30,(float)m.m31,(float)m.m32,(float)m.m33           );
    }
    
    /*@Override
    public Vector3 transform(Vector3 v) {
        return new JmeVector3(matrix4.mult(((JmeVector3)v).vector3));
    }*/

   // @Override
    /*MA16public Matrix4 multiply(Matrix4 v) {
        return new JmeMatrix4(matrix4.mult(((JmeMatrix4)v).matrix4));
    }*/

    //@Override
    public double getElement(int row, int column){
        return matrix4.get(row,column);
    }

    //@Override
    /*MA16public Quaternion extractQuaternion() {
        // 20.4.16 Die eigene Implementierung nehmen, weil die auch bei Unity verwendet wird. Um sie besser zu testen.
        // Die eigene ist eh von JME übernommen.
        //Quaternion q = matrix4.toRotationQuat();
        //q.normalizeLocal();
        //return new JmeQuaternion(q);
        return MathUtil2.extractQuaternion(this);
    }*/

    /*public WebGlMatrix4(WebGlQuaternion quaternion) {
        matrix4 = buildRotationMatrix4FromQuaternion(quaternion.quaternion);
    }*/

    JmeMatrix4(Matrix4f matrix4) {
        this.matrix4 = matrix4;
    }

}
