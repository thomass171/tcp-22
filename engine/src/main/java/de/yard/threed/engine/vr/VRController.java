package de.yard.threed.engine.vr;

import de.yard.threed.core.Matrix4;
import de.yard.threed.core.Point;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeTransform;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.Input;
import de.yard.threed.engine.Observer;
import de.yard.threed.engine.Ray;
import de.yard.threed.engine.Scene;
import de.yard.threed.engine.SceneNode;

import de.yard.threed.core.platform.NativeVRController;
import de.yard.threed.core.Color;
import de.yard.threed.engine.Transform;

/**
 * Good idea to extend SceneNode? Until now it is. But the transforms are taken from nativecontroller!
 * <p>
 * ThreeJs always returns a node that is populated later. This appears to be a general
 * WebXR concept. Controller are not really known until the user enabled VR.
 * <p>
 * Created on 19.10.18.
 */
public class VRController extends SceneNode {
    static Log logger = Platform.getInstance().getLog(VRController.class);

    // will be null for emulated VR
    NativeVRController nativecontroller;
    Vector3 emulatedControllerPosition = null;

    /**
     * Just a sceneNode as dummy(emulated) controller.
     */
    private VRController(Vector3 emulatedControllerPosition) {
        this.nativecontroller = null;
        this.emulatedControllerPosition=emulatedControllerPosition;
    }

    private VRController(NativeVRController nativecontroller) {
        super(nativecontroller);
        this.nativecontroller = nativecontroller;
    }

    public void addLine() {
        SceneNode line = SceneNode.buildLineMesh(new Vector3(0, 0, 0), new Vector3(0, 0, -10), Color.BLUE);
        this.attach(line);
    }

    /*7.10.19 nicht so viel hier drin
    public List<NativeCollision> getIntersections(  ) {
        List<NativeCollision> intersections = getRay().getIntersections();
        //logger.debug("found "+intersections.size()+" intersections from "+worldmatrix.extractPosition());
        return intersections;
    }*/

    public Ray getRay() {
        //aus threejs:
        //tempMatrix.identity().extractRotation( controller.matrixWorld );
        //lineraycaster.ray.origin.setFromMatrixPosition( controller.matrixWorld );
        //lineraycaster.ray.direction.set( 0, 0, -1 ).applyMatrix4( tempMatrix );
        //return lineraycaster.intersectObjects( group.children );
        //25.11.19: Wofuer die -1 ist? Die Defaultblickrichtung? Aber muss rotation nicht auch aus der Worldmatrix kommen?

        if (nativecontroller == null) {
            // dummy/emulated VR controller? Emulate VR ray by mouse position.
            // Not sure if this really works and if its needed.
            Point p = Input.getMouseDown();
            if (p == null) {
                p = Input.getMouseMove();
            }
            if (p == null) {
                return null;
            }
            Ray ray = Observer.getInstance().buildPickingRay(Scene.getCurrent().getDefaultCamera(), p);
            return ray;
        } else {
            NativeTransform transform;
            transform = nativecontroller.getTransform();

            Matrix4 worldmatrix = transform.getWorldModelMatrix();
            Quaternion rotation = transform.getRotation();
            rotation = worldmatrix.extractQuaternion();
            Vector3 direction = new Vector3(0, 0, -1).rotate((rotation));
            Ray ray = new Ray(worldmatrix.extractPosition(), direction);
            return ray;
        }
    }

    public static VRController getController(int index) {
        NativeVRController controller = Platform.getInstance().getVRController(index);
        if (controller == null) {
            return null;

        }
        VRController vc = new VRController(controller);
        vc.addLine();
        logger.debug("Building VRController with line attached");
        return vc;
    }

    public static VRController getEmulatedController(Vector3 emulatedControllerPosition) {

        VRController vc = new VRController(emulatedControllerPosition);
        return vc;
    }

    public Vector3 getPosition() {
        if (nativecontroller == null) {
            return new Vector3();
        }
        Vector3 p = nativecontroller.getTransform().getPosition();
        return p;
    }

    public Vector3 getWorldPosition() {
        if (nativecontroller == null) {
            return emulatedControllerPosition;
        }
        Vector3 p = new Transform(nativecontroller.getTransform()).getWorldPosition();
        //logger.debug("worldposition=" + p);
        return p;
    }
}
