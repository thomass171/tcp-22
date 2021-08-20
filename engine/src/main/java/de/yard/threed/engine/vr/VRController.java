package de.yard.threed.engine.vr;

import de.yard.threed.core.Matrix4;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.Ray;
import de.yard.threed.engine.SceneNode;

import de.yard.threed.core.platform.NativeVRController;
import de.yard.threed.core.Color;

/**
 * ob der extend so gut ist? Bis jetzt ja.
 *
 * zumindest ThreeJs liefert immer eine Node, die dann erst später befüllt wird.
 * Das scheint generell das WebXR Konzept zu sein. Controller sind erst dann wirklich da, wenn der
 * Benutzer VR aktiviert hat.
 *
 * <p>
 * Created on 19.10.18.
 */
public class VRController extends SceneNode {
    NativeVRController nativecontroller;

    private VRController(NativeVRController nativecontroller) {
        super(nativecontroller);
        this.nativecontroller = nativecontroller;
    }

    public void addLine() {
        SceneNode line = SceneNode.buildLineMesh(new Vector3( 0, 0, 0 ), new Vector3( 0, 0, - 10 ), Color.BLUE);
        this.attach(line);
    }

    /*7.10.19 nicht so viel hier drin
    public List<NativeCollision> getIntersections(  ) {
        List<NativeCollision> intersections = getRay().getIntersections();
        //logger.debug("found "+intersections.size()+" intersections from "+worldmatrix.extractPosition());
        return intersections;
    }*/

    public Ray getRay(  ) {
        //aus threejs:
        //tempMatrix.identity().extractRotation( controller.matrixWorld );
        //lineraycaster.ray.origin.setFromMatrixPosition( controller.matrixWorld );
        //lineraycaster.ray.direction.set( 0, 0, -1 ).applyMatrix4( tempMatrix );
        //return lineraycaster.intersectObjects( group.children );
        //25.11.19: Wofuer die -1 ist? Die Defaultblickrichtung? Aber muss rotation nicht auch aus der Worldmatrix kommen?
        Matrix4 worldmatrix = (nativecontroller.getTransform().getWorldModelMatrix());
        Quaternion rotation = nativecontroller.getTransform().getRotation();
        rotation=worldmatrix.extractQuaternion();
        Vector3 direction = new Vector3(0,0,-1).rotate((rotation));
        Ray ray = new Ray(worldmatrix.extractPosition(),direction);
        return ray;
    }
    
    public static VRController getController(int index) {
        NativeVRController controller = Platform.getInstance().getVRController(index);
        if (controller==null){
            return null;
            
        }
        VRController vc = new VRController(controller);
        vc.addLine();
        return vc;
    }
    
    public Vector3 getPosition() {
        return (nativecontroller.getTransform().getPosition());
    }
}
