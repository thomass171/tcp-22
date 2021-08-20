package de.yard.threed.engine;

import de.yard.threed.core.Matrix4;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;

/**
 * Containermodel fuer alles. Wird für evtl. Spiegelung gebraucht.
 * Das ist die (einzige!) RootNode des gesamten SceneGraphs.
 * Singleton. TODO constructor private
 *
 * 7.5.21: Wird bei VR in reference space verschoben. Ehemalige Teile aus Avatar sind jetzt hier (finetune, offset).
 *
 * Created by thomass on 13.04.16.
 */
public class World extends SceneNode {
    boolean righthanded;
    boolean bymatrix;
    Matrix4 mirrormatrix;

    /**
     * Die Default World fuer ein RightHanded System.
     */
    public World() {
        righthanded = true;
        bymatrix = false;
        setName("World");
    }

    /**
     * Ein LeftHanded System.
     */
    public World(boolean bymatrix) {
        getTransform().setScale(new Vector3(1, 1, -1));
        righthanded = false;
        this.bymatrix = true;
        mirrormatrix = new Matrix4(1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, -1, 0,
                0, 0, 0, 1);
        setName("World");

    }

    public Vector3 mirrorZ(Vector3 v) {
        if (righthanded){
            return v;
        }
        return new Vector3(v.getX(), v.getY(), -v.getZ());
    }

    public Vector3 mirrorY(Vector3 v) {
        if (righthanded){
            return v;
        }
        return new Vector3(v.getX(), -v.getY(), v.getZ());
    }

    public float mirrorAngle(float za) {
        if (righthanded)
            return za;
        return -za;
    }

    public Quaternion mirror(Quaternion v) {
        if (righthanded){
            // dann ist nicht weiter zu tun.
            return v;
        }
        //return MathUtil2.multiply(v, mirrormatrix.matrix4);
        //Dieser Algorithmus entsteht durch ausprobieren (RefSceneMovingBox). Er findet sich aber auch im hier und da Internet.
        //Es heissta ber auch, w bleibt unveraendert (http://stackoverflow.com/questions/32438252/efficient-way-to-apply-mirror-effect-on-quaternion-rotation)
        //und nur y (und x?) wird geflippt.
        //return Platform.getInstance().buildQuaternion(-v.getX(),v.getY(),v.getZ(),-v.getW());
        return new Quaternion(-v.getX(),-v.getY(),v.getZ(),v.getW());

    }

    /**
     * Es gibt zwei Varianten, je nach dem ob viewer oder model (?).
     * @param m
     * @return
     */
    public Matrix4 mirror(Matrix4 m) {
        if (righthanded){
            // dann ist nicht weiter zu tun.
            return m;
        }
        // Die Reihenfolge ist noch nicht sicher. Tests scheinen das aber zu bestätigen.
        //return m.multiply(mirrormatrix.matrix4);
        return mirrormatrix.multiply(m);
    }

    /**
     * Es gibt zwei Varianten, je nach dem ob viewer oder model (?).
     * @param m
     * @return
     */
    public Matrix4 mirror2(Matrix4 m) {
        if (righthanded){
            // dann ist nicht weiter zu tun.
            return m;
        }
        // Die Reihenfolge ist noch nicht sicher. Tests scheinen das aber zu bestätigen.
        return m.multiply(mirrormatrix);
        //return mirrormatrix.matrix4.multiply(m);
    }
}
