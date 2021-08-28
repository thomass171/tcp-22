package de.yard.threed.platform.jme;

import com.jme3.math.Quaternion;

/**
 * Created by thomass on 25.05.15.
 * <p/>
 * Auch hier erstmal keine setter
 */
public class JmeQuaternion/* implements Quaternion*/ {
    Quaternion quaternion;

    public JmeQuaternion(double x, double y, double z, double w) {
        quaternion = new Quaternion(x, y, z, w);
    }

    JmeQuaternion(Quaternion q) {
        this.quaternion = q;
    }

    public static Quaternion toJme(de.yard.threed.core.Quaternion v) {
        return  new Quaternion(v.getX(),v.getY(),v.getZ(),v.getW());
    }

    public static de.yard.threed.core.Quaternion fromJme(Quaternion v) {
        return  new de.yard.threed.core.Quaternion(v.getX(),v.getY(),v.getZ(),v.getW());
    }
    
    public double getX() {
        return (quaternion.getX());
    }

    public double getY() {
        return (quaternion.getY());
    }

    public double getZ() {
        return (quaternion.getZ());
    }

    public double getW() {
        return (quaternion.getW());
    }

    public void normalize() {
        // ist normalizeLocal wirklich richtig? Ja, normalisieren in sich selbst.
        quaternion.normalizeLocal();
    }

    //@Override
    /*MA16public Matrix4 buildRotationMatrix() {
       // Matrix4f result = new Matrix4f();
        //return new JmeMatrix4(quaternion.buildRotationMatrix(result));
        return  toRotationMatrix();
    }*/

    /** Das ist aus JME kopiert und laut (http://fabiensanglard.net/doom3_documentation/37726-293748.pdf) angepasst.
     * Die Original scheint mir nicht ganz richtig zu sein. Diese angepasste Version liefert Ergebnisse
     * wie nachgerechnet und ThreeJS.
     *
     * @return
     */
    /*MA16 private Matrix4 toRotationMatrix( ) {
        return MathUtil2.buildRotationMatrix(this);
    }*/
}
