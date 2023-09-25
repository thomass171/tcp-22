package de.yard.threed.engine;

import de.yard.threed.core.*;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.NativeTransform;
import de.yard.threed.core.platform.TransformNodeVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * So eine Klase ist zwar konsequent, erzeugt aber eine Menge Objektoverhead. Erstmal darauf verzichten versuchen. Andererseits leidet die Lesbarkeit.
 * Und fuer sowas wie rotateOnAxis(Degree) ists wieder ganz gut. Überhaupt wegen Convenience Methoden.
 * <p>
 * Fuer einfache Zwecke gibt es noch LocalTransform.
 * <p>
 * 19.5.21: Extracted interface for decoupling.
 * <p>
 * Created by thomass on 26.01.17
 */
public class Transform implements SimpleTransform {
    Log logger = Platform.getInstance().getLog(Transform.class);

    public NativeTransform transform;

    public Transform(NativeTransform t) {
        transform = t;
    }

    public Quaternion getRotation() {
        return (transform.getRotation());
    }

    public Vector3 getPosition() {
        return (transform.getPosition());
    }

    public void setPosition(Vector3 position) {
        /*SceneNode n = getSceneNode();
        String s = "unknown";
        if (n != null) {
            s = "node " + n.getName();
        }
        logger.debug("setPosition of " + s + " to " + position);*/
        transform.setPosition(position);
    }

    public void setRotation(Quaternion quaternion) {
        transform.setRotation(quaternion);
    }

    public Vector3 getScale() {
        return (transform.getScale());
    }

    public void setScale(Vector3 scale) {
        transform.setScale(scale);
    }

    public void translateX(double distance) {
        translateOnAxis(new Vector3(1, 0, 0), distance);
    }

    public void translateY(double distance) {
        translateOnAxis(new Vector3(0, 1, 0), distance);
    }

    public void translateZ(double distance) {
        translateOnAxis(new Vector3(0, 0, 1), distance);
    }

    public void translateOnAxis(Vector3 direction, double distance) {
        transform.translateOnAxis(direction, distance);
    }

    public void rotateX(Degree angle) {
        rotateOnAxis(new Vector3(1, 0, 0), angle);
    }

    public void rotateY(Degree angle) {
        rotateOnAxis(new Vector3(0, 1, 0), angle);
    }

    public void rotateZ(Degree angle) {
        rotateOnAxis(new Vector3(0, 0, 1), angle);
    }

    public void rotateOnAxis(Vector3 axis, Degree angle) {
        transform.rotateOnAxis(axis, (float) angle.toRad());
    }

    /**
     * 06.12.16
     *
     * @param parent
     */
    public void setParent(Transform parent) {
        if (parent == null) {
            transform.setParent(null);
        } else {
            transform.setParent((parent).transform);
        }
    }

    public Transform getParent() {
       /* if (!(object3d instanceof NativeObject3D)) {
            //getclass geht nicht in c# logger.error("object3d instance of "+object3d.getClass().getName());
        }*/
        //return ((NativeObject3D) object3d).getParent();
        NativeTransform parent = (transform).getParent();
        if (parent == null)
            return null;
        return new Transform(parent);
    }

    /**
     * Liefert die Matrix neutral ohne. einer evtl. Handedness Konvertierung.
     * Das mirror erfolgt aber in der Platform.
     *
     * @return
     */
    public Matrix4 getWorldModelMatrix() {
        return ((transform).getWorldModelMatrix());
    }


    public Matrix4 getLocalModelMatrix() {
        return (transform.getLocalModelMatrix());
    }

    public Transform getChild(int index) {
        NativeTransform child = transform.getChild(index);
        if (child == null) {
            logger.error("child isType null.index=" + index);
            return null;
        }
        return new Transform(child);
    }

    public int getChildCount() {
        return (transform).getChildCount();
    }

    public List<Transform> getChildren() {
        List<Transform> l = new ArrayList<Transform>();
        for (NativeTransform nt : (transform).getChildren()) {
            l.add(new Transform(nt));
        }
        return l;
    }

    public SceneNode getSceneNode() {
        return new SceneNode(transform.getSceneNode());
    }


    /**
     * Der Sinn dieser Methode ist fraglich
     *
     * @param v
     */
    public void scale(Vector3 v) {
        //ich mach das mal genauso wie setScale
        //scale = scale.add(v);
        setScale(v);
    }

    public void setPosRot(LocalTransform posRot) {
        setPosition(posRot.position);
        setRotation(posRot.rotation);
    }

    public void setLayer(int layer) {
        transform.setLayer(layer);
    }

    /*public void setLayer(int layer, boolean recursive) {
        if (recursive) {
            getTransform().traverse((node) -> {
                node.getSceneNode().setLayer(layer);
            });
        } else {
            setLayer(layer);
        }
    }*/

    public int getLayer() {
        return transform.getLayer();
    }

    public void setPositionY(double y) {
        Vector3 p = getPosition();
        setPosition(new Vector3(p.getX(), y, p.getZ()));
    }

    /**
     * Move forward. Is always along the foward vector/z-axis in local space. Rotation will do the rest.
     * <p>
     * For the common use case for the method, moving the camera, it is a convenience convention to use a negative offset, which
     * is surprising at first glance.
     * Since the camera looks (in fact its just the view frustum to which the world is transformed) to the negative area of the z-axis,
     * increasing the forward vector (which has a default of (0,0,1) in OpenGL) of a camera would move the camera away from the frustum
     * (in fact its moving the world even far away), a offset of -1 is more intuitive.
     * <p>
     * See Base3DTest.testAndExplainMoveForward for an example.
     * <p>
     * However this intuity doesn't apply to objects. So we will have just two methods.
     * BTW:
     * Unity uses a left handed coordinate system, so in Unity the forward vector is increased for having the same effect. However, since
     * the coordinate system is mirrored in platform-unity to be right handed, its again suitable to decrease the forward vector here.
     * <p>
     * JME uses a reverted forward view vector internally(?), that also is mirrored in platform-jme. So this here also fits.
     */
    public static void moveForward(Transform target, double amount) {
        target.translateOnAxis(new Vector3(0, 0, 1), amount);
    }

    public static void moveForwardAsCamera(Transform target, double amount) {
        target.translateOnAxis(new Vector3(0, 0, -1), amount);
    }

    public static void incPitch(Transform target, Degree inc) {
        target.rotateOnAxis(new Vector3(1, 0, 0), inc);
    }

    public static void incHeading(Transform target, Degree inc) {
        target.rotateOnAxis(new Vector3(0, 1, 0), inc);
    }

    public static void incRoll(Transform target, Degree inc) {
        target.rotateOnAxis(new Vector3(0, 0, 1), inc);
    }


}
